package lux.vexium.app.core.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import lux.vexium.app.feature.auth.presentation.AuthViewModel
import lux.vexium.app.feature.games.presentation.GamesScreen
import lux.vexium.app.feature.home.presentation.HomeScreen
import lux.vexium.app.feature.nft.presentation.NftScreen
import lux.vexium.app.feature.profile.presentation.ProfileScreen
import lux.vexium.app.feature.settings.presentation.GeneralSettingsScreen
import lux.vexium.app.feature.settings.presentation.SettingsViewModel
import lux.vexium.app.feature.splash.presentation.SplashScreen
import lux.vexium.app.feature.splash.presentation.SplashScreenAlt
import lux.vexium.app.feature.trade.presentation.TradeScreen
import lux.vexium.app.feature.welcome.presentation.WelcomeScreen

private const val TAG = "VexiumAuth"

@Composable
fun VexiumNavHost(
    settingsViewModel: SettingsViewModel,
    composeAuth: ComposeAuth,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Navigate to Home when user signs in
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            Log.d(TAG, "✅ User signed in! Navigating to Home")
            val currentRoute = navController.currentDestination?.route
            val isOnAuthScreen = currentRoute == Screen.Welcome::class.qualifiedName
            if (isOnAuthScreen) {
                navController.navigate(Screen.Home) {
                    popUpTo(Screen.Welcome) { inclusive = true }
                }
            }
        }
    }

    // Show auth errors as toast
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Log.e(TAG, "❌ Auth error: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    // Google Sign-In handler
    val googleSignInState = composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            Log.d(TAG, "🔵 Google Sign-In result: $result")
            when (result) {
                is NativeSignInResult.Success -> {
                    Log.d(TAG, "✅ Google Sign-In SUCCESS")
                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                }
                is NativeSignInResult.ClosedByUser -> {
                    Log.d(TAG, "⚪ Google Sign-In cancelled by user")
                    Toast.makeText(context, "Sign in cancelled", Toast.LENGTH_SHORT).show()
                }
                is NativeSignInResult.Error -> {
                    Log.e(TAG, "❌ Google Sign-In ERROR: ${result.message}")
                    Toast.makeText(
                        context,
                        "Sign-In failed: ${result.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
                is NativeSignInResult.NetworkError -> {
                    Log.e(TAG, "❌ Google Sign-In NETWORK ERROR: ${result.message}")
                    Toast.makeText(
                        context,
                        "Network error: ${result.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        },
    )

    // Determine current route for bottom bar
    val currentRoute: Screen? = when (navBackStackEntry?.destination?.route) {
        Screen.Home::class.qualifiedName -> Screen.Home
        Screen.Games::class.qualifiedName -> Screen.Games
        Screen.Nft::class.qualifiedName -> Screen.Nft
        Screen.Trade::class.qualifiedName -> Screen.Trade
        Screen.Profile::class.qualifiedName -> Screen.Profile
        else -> null
    }

    val showBottomBar = currentRoute != null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                VexiumBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            popUpTo(Screen.Home) { saveState = true }
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
            startDestination = Screen.SplashAlt,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── Splash (original) ──
            composable<Screen.Splash> {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    },
                )
            }

            // ── Splash Alt (sphere + sweep) ──
            composable<Screen.SplashAlt> {
                SplashScreenAlt(
                    onSplashFinished = {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(Screen.SplashAlt) { inclusive = true }
                        }
                    },
                )
            }

            // ── Welcome / Auth ──
            composable<Screen.Welcome> {
                WelcomeScreen(
                    onGoogleClick = {
                        Log.d(TAG, "🔵 Starting Google Sign-In flow...")
                        googleSignInState.startFlow()
                    },
                    onTelegramClick = { /* TODO: Telegram auth */ },
                    onEmailClick = { /* TODO: Email auth screen */ },
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

            composable<Screen.Nft> { NftScreen() }
            composable<Screen.Trade> { TradeScreen() }
            composable<Screen.Profile> { ProfileScreen() }

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
