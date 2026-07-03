package lux.obris.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * All navigation routes for Obris.
 * Type-safe navigation using Kotlin Serialization.
 */
sealed interface Screen {

    // ── Splash ──
    @Serializable data object Splash : Screen

    // ── Auth Flow ──
    @Serializable data object Welcome : Screen
    @Serializable data object CreatePin : Screen
    @Serializable data object VerifyPin : Screen
    @Serializable data object AccountCreated : Screen

    // ── Main Tabs ──
    @Serializable data object Home : Screen
    @Serializable data object Games : Screen
    @Serializable data object Profile : Screen

    // ── Game Screens ──
    @Serializable data class GameDetail(val gameId: String) : Screen

    // ── Settings ──
    @Serializable data object Settings : Screen
}
