package lux.obris.app.core.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
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
import lux.obris.app.core.components.ModalLoading
import lux.obris.app.feature.auth.presentation.AccountCreatedScreen
import lux.obris.app.feature.auth.presentation.AuthStep
import lux.obris.app.feature.auth.presentation.AuthViewModel
import lux.obris.app.feature.auth.presentation.BiometricHelper
import lux.obris.app.feature.auth.presentation.BiometricSetupScreen
import lux.obris.app.feature.auth.presentation.PinMode
import lux.obris.app.feature.auth.presentation.PinScreen
import lux.obris.app.feature.auth.presentation.PostSplashDestination
import lux.obris.app.feature.auth.presentation.UsernameSheet
import lux.obris.app.feature.games.presentation.GamesScreen
import lux.obris.app.feature.home.presentation.HomeScreen
import lux.obris.app.feature.profile.presentation.ProfileScreen
import lux.obris.app.feature.settings.presentation.GeneralSettingsScreen
import lux.obris.app.feature.settings.presentation.SettingsViewModel
import lux.obris.app.feature.splash.presentation.SplashScreen
import lux.obris.app.feature.welcome.presentation.WelcomeScreen

private const val TAG = "ObrisNav"

/**
 * Main navigation host for Obris.
 * Handles splash → auth flow → main screens.
 */
@Composable
fun ObrisNavHost(
    settingsViewModel: SettingsViewModel,
    composeAuth: ComposeAuth,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val currentRouteName = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    var showUsernameSheet by remember { mutableStateOf(false) }

    // ════════════════════════════
    // STEP-DRIVEN NAVIGATION
    // ════════════════════════════
    LaunchedEffect(authState.step) {
        Log.d(TAG, "Step: ${authState.step}")
        when (authState.step) {
            AuthStep.USERNAME_PROMPT -> showUsernameSheet = true

            AuthStep.CREATE_PIN -> {
                showUsernameSheet = false
                navController.navigate(Screen.CreatePin) { popUpTo(0) { inclusive = true } }
            }

            AuthStep.VERIFY_PIN -> {
                showUsernameSheet = false
                val onPin = currentRouteName?.contains(Screen.VerifyPin::class.qualifiedName ?: "") == true
                if (!onPin) {
                    navController.navigate(Screen.VerifyPin) { popUpTo(0) { inclusive = true } }
                }
            }

            AuthStep.ACCOUNT_CREATED -> {
                navController.navigate(Screen.AccountCreated) { popUpTo(0) { inclusive = true } }
            }

            AuthStep.COMPLETE -> {
                showUsernameSheet = false
                navController.navigate(Screen.Home) { popUpTo(0) { inclusive = true } }
            }

            AuthStep.IDLE -> {
                val onWelcome = currentRouteName?.contains(Screen.Welcome::class.qualifiedName ?: "") == true
                val onSplash = currentRouteName?.contains("Splash") == true
                if (!onWelcome && !onSplash) {
                    navController.navigate(Screen.Welcome) { popUpTo(0) { inclusive = true } }
                }
            }

            else -> { /* Overlays handle INITIALIZING, AUTHENTICATING, etc. */ }
        }
    }

    // ── Error toasts ──
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

    // ── Username sheet ──
    if (showUsernameSheet && authState.step == AuthStep.USERNAME_PROMPT) {
        UsernameSheet(
            onDismiss = { },
            onSkip = { showUsernameSheet = false; authViewModel.skipUsername() },
            onContinue = { u, r -> showUsernameSheet = false; authViewModel.saveUsername(u, r) },
        )
    }

    // ── Bottom bar (main tabs) ──
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
                        navigateAfterSplash(navController, postDest ?: dest)
                    })
                }

                // ── Welcome / Auth ──
                composable<Screen.Welcome> {
                    WelcomeScreen(
                        onGoogleClick = {
                            authViewModel.onGoogleSignInStarted()
                            googleSignInState.startFlow()
                        },
                        onEmailClick = { /* TODO: Email auth */ },
                        onGuestClick = {
                            navController.navigate(Screen.Home) { popUpTo(0) { inclusive = true } }
                        },
                    )
                }

                // ── PIN ──
                composable<Screen.CreatePin> {
                    PinScreen(
                        mode = PinMode.CREATE,
                        onPinConfirmed = { authViewModel.savePin(it) },
                        onBackClick = { authViewModel.signOut() },
                    )
                }

                composable<Screen.VerifyPin> {
                    PinScreen(
                        mode = PinMode.VERIFY,
                        userName = authState.userName,
                        userInitials = authState.userInitials,
                        onPinConfirmed = { authViewModel.verifyPin(it) },
                        onBiometricClick = if (activity != null && BiometricHelper.canAuthenticate(context)) {
                            {
                                BiometricHelper.authenticate(
                                    activity = activity,
                                    onSuccess = { authViewModel.onBiometricSuccess() },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                                )
                            }
                        } else null,
                        onBackClick = { authViewModel.signOut() },
                        onLogout = { authViewModel.signOut() },
                    )
                }

                // ── Account Created ──
                composable<Screen.AccountCreated> {
                    AccountCreatedScreen(onContinue = { authViewModel.onAccountCreatedContinue() })
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

        // ═══ OVERLAYS ═══
        if (authState.isLoading && authState.step in listOf(AuthStep.AUTHENTICATING, AuthStep.CHECKING_PROFILE)) {
            FullScreenLoading(message = authState.loadingMessage)
        }

        if (authState.isLoading && authState.step !in listOf(AuthStep.AUTHENTICATING, AuthStep.CHECKING_PROFILE, AuthStep.INITIALIZING)) {
            ModalLoading(message = authState.loadingMessage)
        }

        // Biometric setup overlay
        if (authState.step == AuthStep.BIOMETRIC_SETUP) {
            val canBiometric = activity != null && BiometricHelper.canAuthenticate(context)
            if (canBiometric) {
                BiometricSetupScreen(
                    onEnable = {
                        authViewModel.enableBiometric()
                        BiometricHelper.authenticate(
                            activity = activity!!,
                            title = "Enable Biometrics", subtitle = "Verify to enable",
                            onSuccess = { authViewModel.onBiometricSuccess() },
                            onError = { authViewModel.skipBiometric() },
                            onCancel = { authViewModel.skipBiometric() },
                        )
                    },
                    onSkip = { authViewModel.skipBiometric() },
                )
            } else {
                LaunchedEffect(Unit) { authViewModel.skipBiometric() }
            }
        }
    }
}

/** Navigate after splash based on auth state */
private fun navigateAfterSplash(
    navController: androidx.navigation.NavController,
    destination: PostSplashDestination,
) {
    val screen: Screen = when (destination) {
        PostSplashDestination.WELCOME -> Screen.Welcome
        PostSplashDestination.VERIFY_PIN -> Screen.VerifyPin
        PostSplashDestination.HOME -> Screen.Home
    }
    navController.navigate(screen) { popUpTo(0) { inclusive = true } }
}
