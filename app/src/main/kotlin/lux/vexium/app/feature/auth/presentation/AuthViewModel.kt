package lux.vexium.app.feature.auth.presentation

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
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false,
    val needsUsername: Boolean = false,
    val needsPin: Boolean = false,
    val pinVerified: Boolean = false,
    val showBiometricPrompt: Boolean = false,
    val allSetupComplete: Boolean = false,
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
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = authRepository.currentUser()
                        if (user != null) {
                            preferencesManager.saveUserId(user.id)
                            checkUserSetupStatus(user.id)
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignedIn = true,
                        )
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = AuthUiState(isLoading = false, isSignedIn = false)
                    }
                    is SessionStatus.Initializing -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is SessionStatus.RefreshFailure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Session expired. Please sign in again.",
                            isSignedIn = false,
                        )
                    }
                }
            }
        }
    }

    private suspend fun checkUserSetupStatus(userId: String) {
        val profile = authRepository.getProfile(userId)
        val hasUsername = profile?.username != null
        val hasPin = profile?.quickLoginPinHash != null

        preferencesManager.setHasUsername(hasUsername)
        preferencesManager.setHasPin(hasPin)

        _uiState.value = _uiState.value.copy(
            needsUsername = !hasUsername,
            needsPin = !hasPin,
            showBiometricPrompt = false,
            pinVerified = false,
            // NOT complete until PIN is verified (for returning users)
            // or created (for new users)
            allSetupComplete = false,
        )
    }

    fun saveUsername(username: String, referralCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch

                // Look up referrer by code
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
                    needsUsername = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save username",
                )
            }
        }
    }

    fun skipUsername() {
        _uiState.value = _uiState.value.copy(needsUsername = false)
    }

    fun savePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch
                authRepository.savePin(userId, pin)
                preferencesManager.setHasPin(true)
                // PIN saved — now prompt biometric before marking complete
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    needsPin = false,
                    pinVerified = true,
                    showBiometricPrompt = true,
                    allSetupComplete = false, // NOT complete yet — biometric step pending
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = authRepository.currentUser()?.id ?: return@launch
                val valid = authRepository.verifyPin(userId, pin)
                if (valid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pinVerified = true,
                        allSetupComplete = true,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Incorrect PIN. Try again.",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Verification failed",
                )
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(
            pinVerified = true,
            allSetupComplete = true,
        )
    }

    fun enableBiometric() {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(true)
            _uiState.value = _uiState.value.copy(showBiometricPrompt = false)
        }
    }

    fun skipBiometric() {
        _uiState.value = _uiState.value.copy(showBiometricPrompt = false)
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                preferencesManager.clearAll()
                _uiState.value = AuthUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Sign out failed",
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
