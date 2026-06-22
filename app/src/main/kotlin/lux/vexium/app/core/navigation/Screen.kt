package lux.vexium.app.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    // ── Splash ──
    @Serializable data object Splash : Screen
    @Serializable data object SplashAlt : Screen

    // ── Auth Flow ──
    @Serializable data object Welcome : Screen
    @Serializable data object SetupUsername : Screen
    @Serializable data object CreatePin : Screen
    @Serializable data object VerifyPin : Screen
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
