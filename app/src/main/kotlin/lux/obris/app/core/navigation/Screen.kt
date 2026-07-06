package lux.obris.app.core.navigation

import kotlinx.serialization.Serializable

/** All navigation routes for Obris */
sealed interface Screen {
    // ── Boot sequence ──
    @Serializable data object Splash : Screen          // Video splash
    @Serializable data object LoadingFirst : Screen     // First loading screen
    @Serializable data object Welcome : Screen          // Auth screen
    @Serializable data object LoadingFinal : Screen     // Final loading before home

    // ── Main ──
    @Serializable data object Home : Screen
    @Serializable data object Games : Screen
    @Serializable data object Profile : Screen
    @Serializable data class GameDetail(val gameId: String) : Screen
    @Serializable data object Settings : Screen
}
