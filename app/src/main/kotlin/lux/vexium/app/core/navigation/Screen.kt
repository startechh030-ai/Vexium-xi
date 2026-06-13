package lux.vexium.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 */
sealed interface Screen {

    // ── Splash ──
    @Serializable data object Splash : Screen

    // ── Welcome / Auth ──
    @Serializable data object Welcome : Screen
    @Serializable data object Login : Screen
    @Serializable data object Register : Screen

    // ── Bottom Nav Tabs ──
    @Serializable data object Home : Screen
    @Serializable data object Games : Screen
    @Serializable data object Nft : Screen
    @Serializable data object Trade : Screen
    @Serializable data object Profile : Screen

    // ── Game Screens ──
    @Serializable data class GameDetail(val gameId: String) : Screen

    // ── Wallet ──
    @Serializable data object Wallet : Screen

    // ── Settings ──
    @Serializable data object Settings : Screen
}
