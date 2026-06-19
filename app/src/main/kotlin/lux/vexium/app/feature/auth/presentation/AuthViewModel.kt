package lux.vexium.app.feature.auth.presentation

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
import lux.vexium.app.feature.auth.data.AuthRepository
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.NotAuthenticated(false),
        )

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignedIn = true,
                            error = null,
                        )
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignedIn = false,
                        )
                    }
                    is SessionStatus.Initializing -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is SessionStatus.RefreshFailure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Session refresh failed",
                        )
                    }
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                authRepository.signInWithEmail(email, password)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Sign in failed",
                )
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                authRepository.signUpWithEmail(email, password)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Sign up failed",
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
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
