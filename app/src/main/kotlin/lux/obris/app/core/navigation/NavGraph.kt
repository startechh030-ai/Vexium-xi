package lux.obris.app.core.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import lux.obris.app.feature.loading.presentation.LoadingScreen
import lux.obris.app.feature.profile.presentation.ProfileScreen
import lux.obris.app.feature.settings.presentation.GeneralSettingsScreen
import lux.obris.app.feature.settings.presentation.SettingsViewModel
import lux.obris.app.feature.splash.presentation.SplashScreen
import lux.obris.app.feature.welcome.presentation.WelcomeScreen

private const val TAG = "ObrisNav"

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
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

    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            val onWelcome = currentRouteName?.contains(Screen.Welcome::class.qualifiedName ?: "") == true
            if (onWelcome) {
                navController.navigate(Screen.LoadingFinal) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    LaunchedEffect(authState.isSignedIn, authState.postSplashDestination) {
        if (!authState.isSignedIn && authState.postSplashDestination == PostSplashDestination.WELCOME) {
            val onMain = currentRouteName?.contains(Screen.Home::class.qualifiedName ?: "") == true ||
                currentRouteName?.contains(Screen.Games::class.qualifiedName ?: "") == true
            if (onMain) {
                navController.navigate(Screen.Welcome) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<Screen.Splash> {
                SplashScreen(onSplashFinished = {
                    navController.navigate(Screen.LoadingFirst) { popUpTo(0) { inclusive = true } }
                })
            }

            composable<Screen.LoadingFirst> {
                LoadingScreen(
                    statusMessages = listOf("Initializing...", "Loading assets...", "Connecting to server...", "Preparing environment..."),
                    durationMs = 3000L,
                    onLoadingComplete = {
                        val dest = authViewModel.onSplashCompleted()
                        if (dest == PostSplashDestination.HOME) {
                            navController.navigate(Screen.LoadingFinal) { popUpTo(0) { inclusive = true } }
                        } else {
                            navController.navigate(Screen.Welcome) { popUpTo(0) { inclusive = true } }
                        }
                    },
                )
            }

            composable<Screen.Welcome> {
                WelcomeScreen(
                    onGoogleClick = { authViewModel.onGoogleSignInStarted(); googleSignInState.startFlow() },
                    onEmailClick = { },
                    onGuestClick = { navController.navigate(Screen.LoadingFinal) { popUpTo(0) { inclusive = true } } },
                )
            }

            composable<Screen.LoadingFinal> {
                LoadingScreen(
                    statusMessages = listOf("Preparing the world...", "Loading game data...", "Almost ready..."),
                    durationMs = 2500L,
                    onLoadingComplete = {
                        navController.navigate(Screen.Home) { popUpTo(0) { inclusive = true } }
                    },
                )
            }

            composable<Screen.Home> {
                Scaffold(
                    bottomBar = {
                        ObrisBottomBar(currentRoute = Screen.Home, onNavigate = { screen ->
                            navController.navigate(screen) { popUpTo(Screen.Home) { saveState = true }; launchSingleTop = true; restoreState = true }
                        })
                    },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        HomeScreen(
                            onNavigateToGames = { navController.navigate(Screen.Games) },
                            onLogout = { authViewModel.signOut() },
                        )
                    }
                }
            }

            composable<Screen.Games> {
                Scaffold(
                    bottomBar = {
                        ObrisBottomBar(currentRoute = Screen.Games, onNavigate = { screen ->
                            navController.navigate(screen) { popUpTo(Screen.Home) { saveState = true }; launchSingleTop = true; restoreState = true }
                        })
                    },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        GamesScreen(onNavigateToGameDetail = { navController.navigate(Screen.GameDetail(it)) })
                    }
                }
            }

            composable<Screen.Profile> {
                Scaffold(
                    bottomBar = {
                        ObrisBottomBar(currentRoute = Screen.Profile, onNavigate = { screen ->
                            navController.navigate(screen) { popUpTo(Screen.Home) { saveState = true }; launchSingleTop = true; restoreState = true }
                        })
                    },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        ProfileScreen()
                    }
                }
            }

            composable<Screen.Settings> {
                GeneralSettingsScreen(settingsViewModel, onNavigateBack = { navController.popBackStack() })
            }
        }

        if (authState.isLoading) {
            FullScreenLoading(message = authState.loadingMessage)
        }
    }
}
