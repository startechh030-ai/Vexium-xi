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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lux.vexium.app.data.local.PreferencesManager
import lux.vexium.app.feature.auth.data.AuthRepository
import lux.vexium.app.feature.auth.data.ProfileUpdate
import javax.inject.Inject

private const val TAG = "VexiumAuth"

enum class AuthStep {
    INITIALIZING,       // App just opened, waiting
    IDLE,               // Not signed in
    AUTHENTICATING,     // Google sign-in in progress
    CHECKING_PROFILE,   // Fetching profile from Supabase
    USERNAME_PROMPT,    // New user — show username sheet
    CREATE_PIN,         // New user — create 6-digit PIN
    BIOMETRIC_SETUP,    // Prompt to enable biometrics
    VERIFY_PIN,         // Returning user with existing session — enter PIN
    COMPLETE,           // All done — go home
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
    val freshLogin: Boolean = false, // true = user JUST signed in this session (not restored)
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Track if splash is done — don't navigate until splash finishes
    private var splashCompleted = false

    // Track if this is a fresh sign-in vs restored session
    private var isFreshSignIn = false

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.Initializing,
        )

    init {
        observeSession()
    }

    fun onSplashCompleted() {
        splashCompleted = true
        // If auth state was already determined, process it now
        if (_uiState.value.step == AuthStep.INITIALIZING && _uiState.value.isSignedIn) {
            viewModelScope.launch {
                val userId = authRepository.currentUser()?.id ?: return@launch
                processAuthenticatedUser(userId)
            }
        } else if (_uiState.value.step == AuthStep.INITIALIZING) {
            _uiState.value = _uiState.value.copy(step = AuthStep.IDLE)
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                Log.d(TAG, "Session: $status, splashDone=$splashCompleted, freshLogin=$isFreshSignIn")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = authRepository.currentUser() ?: return@collect
                        preferencesManager.saveUserId(user.id)

                        _uiState.value = _uiState.value.copy(isSignedIn = true)

                        if (splashCompleted) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                loadingMessage = "Setting up...",
                                step = AuthStep.CHECKING_PROFILE,
                            )
                            processAuthenticatedUser(user.id)
                        }
                        // If splash not done yet, onSplashCompleted will handle it
                    }
                    is SessionStatus.NotAuthenticated -> {
                        if (splashCompleted) {
                            _uiState.value = AuthUiState(step = AuthStep.IDLE)
                        } else {
                            _uiState.value = AuthUiState(step = AuthStep.INITIALIZING)
                        }
                        isFreshSignIn = false
                    }
                    is SessionStatus.Initializing -> {
                        // Silent
                    }
                    is SessionStatus.RefreshFailure -> {
                        if (splashCompleted) {
                            _uiState.value = AuthUiState(step = AuthStep.IDLE)
                        }
                    }
                }
            }
        }
    }

    private suspend fun processAuthenticatedUser(userId: String) {
        try {
            val profile = authRepository.getProfile(userId)
            val hasUsername = profile?.username != null
            val hasPin = profile?.quickLoginPinHash != null

            preferencesManager.setHasUsername(hasUsername)
            preferencesManager.setHasPin(hasPin)

            val displayName = profile?.fullName ?: profile?.username ?: "User"
            val initials = displayName.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")
                .ifEmpty { "U" }

            Log.d(TAG, "Profile: username=$hasUsername, pin=$hasPin, fresh=$isFreshSignIn")

            val nextStep = when {
                // Fresh login (user just tapped Google) + existing account → skip everything
                isFreshSignIn && hasPin -> {
                    isFreshSignIn = false
                    AuthStep.COMPLETE
                }
                // New user — needs username
                !hasUsername && isFreshSignIn -> AuthStep.USERNAME_PROMPT
                // Needs PIN creation
                !hasPin -> AuthStep.CREATE_PIN
                // Restored session (app reopened) — verify PIN
                hasPin && !isFreshSignIn -> AuthStep.VERIFY_PIN
                // Fallback
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
            Log.e(TAG, "Profile check failed: ${e.message}")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = if (isFreshSignIn) AuthStep.USERNAME_PROMPT else AuthStep.COMPLETE,
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
            isLoading = false,
            step = AuthStep.IDLE,
            error = message,
            freshLogin = false,
        )
    }

    fun onGoogleSignInCancelled() {
        isFreshSignIn = false
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            step = AuthStep.IDLE,
            freshLogin = false,
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
                    username = username.ifBlank { null },
                    referredBy = referredBy,
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
        _uiState.value = _uiState.value.copy(step = AuthStep.COMPLETE)
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Signing out...")
            try {
                authRepository.signOut()
                preferencesManager.clearAll()
            } catch (_: Exception) { }
            isFreshSignIn = false
            _uiState.value = AuthUiState(step = AuthStep.IDLE)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
