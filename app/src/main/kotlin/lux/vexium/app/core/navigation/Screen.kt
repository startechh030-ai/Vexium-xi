package lux.vexium.app.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Splash : Screen
    @Serializable data object SplashAlt : Screen

    @Serializable data object Welcome : Screen
    @Serializable data object TelegramCode : Screen
    @Serializable data object SetupUsername : Screen
    @Serializable data object CreatePin : Screen
    @Serializable data object VerifyPin : Screen
    @Serializable data object AccountCreated : Screen
    @Serializable data object Login : Screen
    @Serializable data object Register : Screen

    @Serializable data object Home : Screen
    @Serializable data object Games : Screen
    @Serializable data object Nft : Screen
    @Serializable data object Trade : Screen
    @Serializable data object Profile : Screen

    @Serializable data class GameDetail(val gameId: String) : Screen
    @Serializable data object Wallet : Screen
    @Serializable data object Settings : Screen
}
