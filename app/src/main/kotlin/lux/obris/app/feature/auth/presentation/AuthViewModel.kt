package lux.obris.app.feature.auth.presentation

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
import lux.obris.app.data.local.PreferencesManager
import lux.obris.app.feature.auth.data.AuthRepository
import javax.inject.Inject

private const val TAG = "ObrisAuth"

/**
 * Simple auth state — signed in or not.
 * No PIN, no biometrics — just Google auth + session.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Loading...",
    val error: String? = null,
    val isSignedIn: Boolean = false,
    // Where to go after splash
    val postSplashDestination: PostSplashDestination? = null,
)

enum class PostSplashDestination {
    WELCOME,  // Not signed in
    HOME,     // Already signed in
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var sessionResolved = false

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionStatus.Initializing)

    init { observeSession() }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                Log.d(TAG, "Session: $status")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = authRepository.currentUser() ?: return@collect
                        preferencesManager.saveUserId(user.id)
                        sessionResolved = true
                        _uiState.value = _uiState.value.copy(
                            isSignedIn = true,
                            isLoading = false,
                            postSplashDestination = PostSplashDestination.HOME,
                        )
                    }
                    is SessionStatus.NotAuthenticated -> {
                        sessionResolved = true
                        _uiState.value = _uiState.value.copy(
                            isSignedIn = false,
                            isLoading = false,
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

    /** Called when splash finishes — returns where to go */
    fun onSplashCompleted(): PostSplashDestination {
        return _uiState.value.postSplashDestination ?: PostSplashDestination.WELCOME
    }

    fun onGoogleSignInStarted() {
        _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Signing in...")
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = message)
    }

    fun onGoogleSignInCancelled() {
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Signing out...")
            try {
                authRepository.signOut()
                preferencesManager.clearAll()
            } catch (_: Exception) { }
            sessionResolved = false
            _uiState.value = AuthUiState(
                isSignedIn = false,
                postSplashDestination = PostSplashDestination.WELCOME,
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
