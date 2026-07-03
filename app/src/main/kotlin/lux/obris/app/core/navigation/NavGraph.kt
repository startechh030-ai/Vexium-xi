package lux.obris.app.core.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
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
import lux.obris.app.core.components.FullScreenLoading
import lux.obris.app.feature.auth.presentation.AuthViewModel
import lux.obris.app.feature.auth.presentation.PostSplashDestination
import lux.obris.app.feature.games.presentation.GamesScreen
import lux.obris.app.feature.home.presentation.HomeScreen
import lux.obris.app.feature.profile.presentation.ProfileScreen
import lux.obris.app.feature.settings.presentation.GeneralSettingsScreen
import lux.obris.app.feature.settings.presentation.SettingsViewModel
import lux.obris.app.feature.splash.presentation.SplashScreen
import lux.obris.app.feature.welcome.presentation.WelcomeScreen

private const val TAG = "ObrisNav"

/**
 * Obris navigation — simple flow:
 * Splash → Welcome (if not signed in) or Home (if signed in)
 * Google auth → straight to Home
 */
@Composable
fun ObrisNavHost(
    settingsViewModel: SettingsViewModel,
    composeAuth: ComposeAuth,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val currentRouteName = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // ── Auto-navigate when sign-in completes ──
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            val onWelcome = currentRouteName?.contains(Screen.Welcome::class.qualifiedName ?: "") == true
            if (onWelcome) {
                Log.d(TAG, "Signed in → Home")
                navController.navigate(Screen.Home) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    // ── Auto-navigate on sign-out ──
    LaunchedEffect(authState.isSignedIn, authState.postSplashDestination) {
        if (!authState.isSignedIn && authState.postSplashDestination == PostSplashDestination.WELCOME) {
            val onHome = currentRouteName?.contains(Screen.Home::class.qualifiedName ?: "") == true ||
                currentRouteName?.contains(Screen.Games::class.qualifiedName ?: "") == true ||
                currentRouteName?.contains(Screen.Profile::class.qualifiedName ?: "") == true
            if (onHome) {
                navController.navigate(Screen.Welcome) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    // ── Errors ──
    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    // ── Google Sign-In ──
    val googleSignInState = composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> Log.d(TAG, "Google ✅")
                is NativeSignInResult.ClosedByUser -> authViewModel.onGoogleSignInCancelled()
                is NativeSignInResult.Error -> authViewModel.onGoogleSignInFailed(result.message)
                is NativeSignInResult.NetworkError -> authViewModel.onGoogleSignInFailed("Network error")
            }
        },
    )

    // ── Bottom bar ──
    val currentRouteScreen: Screen? = when {
        currentRouteName?.contains(Screen.Home::class.qualifiedName ?: "") == true -> Screen.Home
        currentRouteName?.contains(Screen.Games::class.qualifiedName ?: "") == true -> Screen.Games
        currentRouteName?.contains(Screen.Profile::class.qualifiedName ?: "") == true -> Screen.Profile
        else -> null
    }

    Box {
        Scaffold(
            bottomBar = {
                if (currentRouteScreen != null) {
                    ObrisBottomBar(
                        currentRoute = currentRouteScreen,
                        onNavigate = { screen ->
                            navController.navigate(screen) {
                                popUpTo(Screen.Home) { saveState = true }
                                launchSingleTop = true; restoreState = true
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
                    val postDest = authState.postSplashDestination
                    SplashScreen(onSplashFinished = {
                        val dest = authViewModel.onSplashCompleted()
                        val target: Screen = when (postDest ?: dest) {
                            PostSplashDestination.HOME -> Screen.Home
                            PostSplashDestination.WELCOME -> Screen.Welcome
                        }
                        navController.navigate(target) { popUpTo(0) { inclusive = true } }
                    })
                }

                // ── Welcome ──
                composable<Screen.Welcome> {
                    WelcomeScreen(
                        onGoogleClick = {
                            authViewModel.onGoogleSignInStarted()
                            googleSignInState.startFlow()
                        },
                        onEmailClick = { /* TODO */ },
                        onGuestClick = {
                            navController.navigate(Screen.Home) { popUpTo(0) { inclusive = true } }
                        },
                    )
                }

                // ── Main Tabs ──
                composable<Screen.Home> {
                    HomeScreen(
                        onNavigateToGames = { navController.navigate(Screen.Games) },
                        onLogout = { authViewModel.signOut() },
                    )
                }

                composable<Screen.Games> {
                    GamesScreen(onNavigateToGameDetail = { navController.navigate(Screen.GameDetail(it)) })
                }

                composable<Screen.Profile> { ProfileScreen() }

                composable<Screen.Settings> {
                    GeneralSettingsScreen(settingsViewModel, onNavigateBack = { navController.popBackStack() })
                }
            }
        }

        // ── Loading overlay ──
        if (authState.isLoading) {
            FullScreenLoading(message = authState.loadingMessage)
        }
    }
}
