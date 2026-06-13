package lux.vexium.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import lux.vexium.app.feature.games.presentation.GamesScreen
import lux.vexium.app.feature.home.presentation.HomeScreen
import lux.vexium.app.feature.nft.presentation.NftScreen
import lux.vexium.app.feature.profile.presentation.ProfileScreen
import lux.vexium.app.feature.settings.presentation.GeneralSettingsScreen
import lux.vexium.app.feature.settings.presentation.SettingsViewModel
import lux.vexium.app.feature.splash.presentation.SplashScreen
import lux.vexium.app.feature.trade.presentation.TradeScreen
import lux.vexium.app.feature.welcome.presentation.WelcomeScreen

@Composable
fun VexiumNavHost(
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Determine current route for bottom bar highlighting
    val currentRoute: Screen? = when (navBackStackEntry?.destination?.route) {
        Screen.Home::class.qualifiedName -> Screen.Home
        Screen.Games::class.qualifiedName -> Screen.Games
        Screen.Nft::class.qualifiedName -> Screen.Nft
        Screen.Trade::class.qualifiedName -> Screen.Trade
        Screen.Profile::class.qualifiedName -> Screen.Profile
        else -> null
    }

    // Show bottom bar only on main tabs
    val showBottomBar = currentRoute != null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                VexiumBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            popUpTo(Screen.Home) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── Splash ──
            composable<Screen.Splash> {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    },
                )
            }

            // ── Welcome / Auth ──
            composable<Screen.Welcome> {
                WelcomeScreen(
                    onGoogleClick = { /* TODO: Google sign in */ },
                    onTelegramClick = { /* TODO: Telegram auth */ },
                    onEmailClick = { /* TODO: Email auth */ },
                    onGuestClick = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Welcome) { inclusive = true }
                        }
                    },
                )
            }

            // ── Main Tabs ──
            composable<Screen.Home> {
                HomeScreen(
                    onNavigateToGames = { navController.navigate(Screen.Games) },
                    onNavigateToWallet = { navController.navigate(Screen.Wallet) },
                )
            }

            composable<Screen.Games> {
                GamesScreen(
                    onNavigateToGameDetail = { gameId ->
                        navController.navigate(Screen.GameDetail(gameId))
                    },
                )
            }

            composable<Screen.Nft> {
                NftScreen()
            }

            composable<Screen.Trade> {
                TradeScreen()
            }

            composable<Screen.Profile> {
                ProfileScreen()
            }

            // ── Settings ──
            composable<Screen.Settings> {
                GeneralSettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
