package lux.vexium.app.feature.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lux.vexium.app.data.local.PreferencesManager
import lux.vexium.app.feature.auth.data.AuthRepository
import lux.vexium.app.feature.auth.data.ProfileUpdate
import lux.vexium.app.feature.auth.data.SecurityManager
import javax.inject.Inject

private const val TAG = "VexiumAuth"

enum class AuthStep {
    INITIALIZING,
    IDLE,
    AUTHENTICATING,
    CHECKING_PROFILE,
    USERNAME_PROMPT,
    CREATE_PIN,
    BIOMETRIC_SETUP,
    VERIFY_PIN,
    ACCOUNT_CREATED,
    COMPLETE,
}

data class AuthUiState(
    val step: AuthStep = AuthStep.INITIALIZING,
    val isLoading: Boolean = false,
    val loadingMessage: String = "Loading...",
    val error: String? = null,
    val isSignedIn: Boolean = false,
    val userName: String? = null,
    val userInitials: String? = null,
    val avatarUrl: String? = null,
    val freshLogin: Boolean = false,
    val isNewUser: Boolean = false,
    // Where to go after splash: null = still determining
    val postSplashDestination: PostSplashDestination? = null,
)

enum class PostSplashDestination {
    WELCOME,    // Not signed in → show welcome
    VERIFY_PIN, // Signed in, has PIN → verify
    HOME,       // Signed in, fresh login, existing user → straight home
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val securityManager: SecurityManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var isFreshSignIn = false
    private var sessionResolved = false

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.Initializing,
        )

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                Log.d(TAG, "Session: $status")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = authRepository.currentUser() ?: return@collect
                        preferencesManager.saveUserId(user.id)

                        _uiState.value = _uiState.value.copy(isSignedIn = true)

                        if (!sessionResolved) {
                            // First time session resolved (during splash)
                            sessionResolved = true
                            determineSplashDestination(user.id)
                        } else if (isFreshSignIn) {
                            // User just signed in via Google
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                loadingMessage = "Setting up...",
                                step = AuthStep.CHECKING_PROFILE,
                            )
                            processAuthenticatedUser(user.id)
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        sessionResolved = true
                        isFreshSignIn = false
                        _uiState.value = _uiState.value.copy(
                            isSignedIn = false,
                            step = if (_uiState.value.step == AuthStep.INITIALIZING) AuthStep.INITIALIZING else AuthStep.IDLE,
                            postSplashDestination = PostSplashDestination.WELCOME,
                        )
                    }
                    is SessionStatus.Initializing -> { /* wait */ }
                    is SessionStatus.RefreshFailure -> {
                        sessionResolved = true
                        _uiState.value = _uiState.value.copy(
                            isSignedIn = false,
                            postSplashDestination = PostSplashDestination.WELCOME,
                        )
                    }
                }
            }
        }
    }

    /**
     * Called DURING splash to determine where to go after.
     * This way splash shows until we know exactly where to navigate.
     */
    private suspend fun determineSplashDestination(userId: String) {
        // Try local first (works offline)
        val localPinHash = preferencesManager.getLocalPinHash()
        val localHasPin = localPinHash != null
        val localName = preferencesManager.getUserDisplayName()
        val localInitials = preferencesManager.getUserInitials()

        if (localHasPin && localName != null) {
            // We have cached data — go straight to PIN (even offline)
            _uiState.value = _uiState.value.copy(
                postSplashDestination = PostSplashDestination.VERIFY_PIN,
                userName = localName,
                userInitials = localInitials ?: "U",
                avatarUrl = preferencesManager.getUserAvatarUrl(),
            )
            return
        }

        // No local cache — try Supabase (requires network)
        try {
            val profile = authRepository.getProfile(userId)
            val hasPin = profile?.quickLoginPinHash != null

            val displayName = profile?.fullName ?: profile?.username ?: "User"
            val initials = displayName.split(" ")
                .take(2).mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("").ifEmpty { "U" }

            // Cache locally for offline use
            preferencesManager.saveUserDisplayInfo(displayName, initials, profile?.avatarUrl)
            if (hasPin && profile?.quickLoginPinHash != null) {
                preferencesManager.savePinHashLocally(profile.quickLoginPinHash)
            }

            if (hasPin) {
                _uiState.value = _uiState.value.copy(
                    postSplashDestination = PostSplashDestination.VERIFY_PIN,
                    userName = displayName,
                    userInitials = initials,
                    avatarUrl = profile?.avatarUrl,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    postSplashDestination = PostSplashDestination.WELCOME,
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Splash destination check failed: ${e.message}")
            // Offline and no local cache — go to welcome
            _uiState.value = _uiState.value.copy(
                postSplashDestination = if (localHasPin) PostSplashDestination.VERIFY_PIN else PostSplashDestination.WELCOME,
            )
        }
    }

    /**
     * Called when splash animation completes.
     * Uses the pre-determined destination.
     */
    fun onSplashCompleted(): PostSplashDestination {
        val dest = _uiState.value.postSplashDestination
        if (dest == PostSplashDestination.VERIFY_PIN) {
            _uiState.value = _uiState.value.copy(step = AuthStep.VERIFY_PIN)
        } else {
            _uiState.value = _uiState.value.copy(step = AuthStep.IDLE)
        }
        return dest ?: PostSplashDestination.WELCOME
    }

    /**
     * Process after fresh Google sign-in
     */
    private suspend fun processAuthenticatedUser(userId: String) {
        try {
            // Retry profile fetch — Supabase trigger may not have created the row yet
            var profile = authRepository.getProfile(userId)
            var retries = 0
            while (profile == null && retries < 5) {
                retries++
                Log.d(TAG, "Profile not found, retry $retries/5...")
                kotlinx.coroutines.delay(800L)
                profile = authRepository.getProfile(userId)
            }

            val hasUsername = profile?.username != null
            val hasPin = profile?.quickLoginPinHash != null

            preferencesManager.setHasUsername(hasUsername)
            preferencesManager.setHasPin(hasPin)

            val displayName = profile?.fullName ?: profile?.username ?: "User"
            val initials = displayName.split(" ")
                .take(2).mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("").ifEmpty { "U" }

            // Cache user info locally for offline
            preferencesManager.saveUserDisplayInfo(displayName, initials, profile?.avatarUrl)

            Log.d(TAG, "Profile: user=$hasUsername pin=$hasPin fresh=$isFreshSignIn")

            // Verify device session with Edge Function (background, non-blocking)
            viewModelScope.launch {
                try {
                    securityManager.verifySession()
                } catch (e: Exception) {
                    Log.w(TAG, "Security verify skipped: ${e.message}")
                }
            }

            val nextStep = when {
                // Existing user with PIN → skip everything, straight home
                isFreshSignIn && hasPin -> {
                    isFreshSignIn = false
                    AuthStep.COMPLETE
                }
                // New user → username
                !hasUsername && isFreshSignIn -> {
                    _uiState.value = _uiState.value.copy(isNewUser = true)
                    AuthStep.USERNAME_PROMPT
                }
                // Has username but no PIN
                !hasPin -> AuthStep.CREATE_PIN
                else -> AuthStep.COMPLETE
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = nextStep,
                userName = displayName,
                userInitials = initials,
                avatarUrl = profile?.avatarUrl,
                freshLogin = isFreshSignIn,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Process user failed: ${e.message}")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = if (isFreshSignIn) AuthStep.USERNAME_PROMPT else AuthStep.COMPLETE,
                isNewUser = isFreshSignIn,
            )
        }
    }

    fun onGoogleSignInStarted() {
        isFreshSignIn = true
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            loadingMessage = "Signing in...",
            step = AuthStep.AUTHENTICATING,
            freshLogin = true,
        )
    }

    fun onGoogleSignInFailed(message: String) {
        isFreshSignIn = false
        _uiState.value = _uiState.value.copy(
            isLoading = false, step = AuthStep.IDLE, error = message, freshLogin = false,
        )
    }

    fun onGoogleSignInCancelled() {
        isFreshSignIn = false
        _uiState.value = _uiState.value.copy(
            isLoading = false, step = AuthStep.IDLE, freshLogin = false,
        )
    }

    fun saveUsername(username: String, referralCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Saving profile...")
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch
                var referredBy: String? = null
                if (referralCode.isNotBlank()) {
                    val referrer = authRepository.findUserByReferralCode(referralCode)
                    referredBy = referrer?.id
                }
                authRepository.updateProfile(userId, ProfileUpdate(
                    username = username.ifBlank { null }, referredBy = referredBy,
                ))
                preferencesManager.setHasUsername(true)
                _uiState.value = _uiState.value.copy(isLoading = false, step = AuthStep.CREATE_PIN)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun skipUsername() {
        _uiState.value = _uiState.value.copy(step = AuthStep.CREATE_PIN)
    }

    fun savePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Securing account...")
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch
                authRepository.savePin(userId, pin)
                preferencesManager.setHasPin(true)
                _uiState.value = _uiState.value.copy(isLoading = false, step = AuthStep.BIOMETRIC_SETUP)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Verifying...")
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch
                if (authRepository.verifyPin(userId, pin)) {
                    _uiState.value = _uiState.value.copy(isLoading = false, step = AuthStep.COMPLETE)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Incorrect PIN")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Verification failed")
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(step = AuthStep.COMPLETE)
    }

    fun enableBiometric() {
        viewModelScope.launch { preferencesManager.setBiometricEnabled(true) }
    }

    fun skipBiometric() {
        // If new user, show account created screen. Otherwise straight home.
        if (_uiState.value.isNewUser) {
            _uiState.value = _uiState.value.copy(step = AuthStep.ACCOUNT_CREATED)
        } else {
            _uiState.value = _uiState.value.copy(step = AuthStep.COMPLETE)
        }
    }

    fun onAccountCreatedContinue() {
        _uiState.value = _uiState.value.copy(step = AuthStep.COMPLETE, isNewUser = false)
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Signing out...")
            try {
                securityManager.clearSession()
                authRepository.signOut()
                preferencesManager.clearAll()
            } catch (_: Exception) { }
            isFreshSignIn = false
            sessionResolved = false
            _uiState.value = AuthUiState(step = AuthStep.IDLE, postSplashDestination = PostSplashDestination.WELCOME)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
