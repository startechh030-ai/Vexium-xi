package lux.vexium.app.core.common

/**
 * One-time UI events (snackbar, navigation, etc.)
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    data object NavigateBack : UiEvent()
}
