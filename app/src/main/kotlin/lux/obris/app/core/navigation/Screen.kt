package lux.obris.app.core.navigation

import kotlinx.serialization.Serializable

/** All navigation routes for Obris */
sealed interface Screen {
    @Serializable data object Splash : Screen
    @Serializable data object Welcome : Screen
    @Serializable data object Home : Screen
    @Serializable data object Games : Screen
    @Serializable data object Profile : Screen
    @Serializable data class GameDetail(val gameId: String) : Screen
    @Serializable data object Settings : Screen
}
