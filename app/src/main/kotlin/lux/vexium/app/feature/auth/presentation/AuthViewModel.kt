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
    IDLE,               // Not signed in, nothing happening
    AUTHENTICATING,     // Google sign-in in progress
    CHECKING_PROFILE,   // Fetching profile from Supabase
    USERNAME_PROMPT,    // New user — show username sheet
    CREATE_PIN,         // New user — create 6-digit PIN
    BIOMETRIC_SETUP,    // Prompt to enable biometrics
    VERIFY_PIN,         // Returning user — enter PIN
    COMPLETE,           // All done — go to home
}

data class AuthUiState(
    val step: AuthStep = AuthStep.IDLE,
    val isLoading: Boolean = false,
    val loadingMessage: String = "Loading...",
    val error: String? = null,
    val isSignedIn: Boolean = false,
    val isExistingUser: Boolean = false, // user already had account before this sign-in
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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

                        _uiState.value = _uiState.value.copy(
                            isSignedIn = true,
                            isLoading = true,
                            loadingMessage = "Setting up...",
                            step = AuthStep.CHECKING_PROFILE,
                        )

                        checkUserSetup(user.id)
                    }
                    is SessionStatus.NotAuthenticated -> {
                        // Only reset if we were previously signed in
                        // Don't show "session expired" on cold start
                        if (_uiState.value.isSignedIn) {
                            _uiState.value = AuthUiState(step = AuthStep.IDLE)
                        }
                    }
                    is SessionStatus.Initializing -> {
                        // Silent — don't show loading on cold start
                    }
                    is SessionStatus.RefreshFailure -> {
                        // Silent — don't toast "session expired"
                        // Just reset to not signed in
                        _uiState.value = AuthUiState(step = AuthStep.IDLE)
                    }
                }
            }
        }
    }

    private suspend fun checkUserSetup(userId: String) {
        try {
            val profile = authRepository.getProfile(userId)
            val hasUsername = profile?.username != null
            val hasPin = profile?.quickLoginPinHash != null

            preferencesManager.setHasUsername(hasUsername)
            preferencesManager.setHasPin(hasPin)

            Log.d(TAG, "Profile: username=$hasUsername, pin=$hasPin")

            val nextStep = when {
                // Existing user with everything set — verify PIN
                hasUsername && hasPin -> {
                    _uiState.value = _uiState.value.copy(isExistingUser = true)
                    AuthStep.VERIFY_PIN
                }
                // Has account but no username yet
                !hasUsername -> AuthStep.USERNAME_PROMPT
                // Has username but no PIN
                !hasPin -> AuthStep.CREATE_PIN
                else -> AuthStep.COMPLETE
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = nextStep,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Profile check failed: ${e.message}")
            // If profile check fails (maybe table not set up), go to username
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = AuthStep.USERNAME_PROMPT,
            )
        }
    }

    fun onGoogleSignInStarted() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            loadingMessage = "Signing in...",
            step = AuthStep.AUTHENTICATING,
        )
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            step = AuthStep.IDLE,
            error = message,
        )
    }

    fun onGoogleSignInCancelled() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            step = AuthStep.IDLE,
        )
    }

    fun saveUsername(username: String, referralCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Saving profile...",
            )
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch

                var referredBy: String? = null
                if (referralCode.isNotBlank()) {
                    val referrer = authRepository.findUserByReferralCode(referralCode)
                    referredBy = referrer?.id
                }

                authRepository.updateProfile(
                    userId = userId,
                    update = ProfileUpdate(
                        username = username.ifBlank { null },
                        referredBy = referredBy,
                    ),
                )

                preferencesManager.setHasUsername(true)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = AuthStep.CREATE_PIN,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save",
                )
            }
        }
    }

    fun skipUsername() {
        _uiState.value = _uiState.value.copy(step = AuthStep.CREATE_PIN)
    }

    fun savePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Securing your account...",
            )
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch
                authRepository.savePin(userId, pin)
                preferencesManager.setHasPin(true)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = AuthStep.BIOMETRIC_SETUP,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save PIN",
                )
            }
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Verifying...")
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch
                val valid = authRepository.verifyPin(userId, pin)
                if (valid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = AuthStep.COMPLETE,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Incorrect PIN",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Verification failed",
                )
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(step = AuthStep.COMPLETE)
    }

    fun enableBiometric() {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(true)
        }
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
            _uiState.value = AuthUiState(step = AuthStep.IDLE)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
