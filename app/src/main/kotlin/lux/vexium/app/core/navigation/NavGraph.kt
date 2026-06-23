package lux.vexium.app.core.navigation

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
import lux.vexium.app.core.components.FullScreenLoading
import lux.vexium.app.core.components.ModalLoading
import lux.vexium.app.feature.auth.presentation.AuthStep
import lux.vexium.app.feature.auth.presentation.AuthViewModel
import lux.vexium.app.feature.auth.presentation.BiometricHelper
import lux.vexium.app.feature.auth.presentation.BiometricSetupScreen
import lux.vexium.app.feature.auth.presentation.PinMode
import lux.vexium.app.feature.auth.presentation.PinScreen
import lux.vexium.app.feature.auth.presentation.UsernameSheet
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
    val currentRouteName = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    var showUsernameSheet by remember { mutableStateOf(false) }

    // ════════════════════════════════════
    // STEP-DRIVEN NAVIGATION
    // ════════════════════════════════════
    LaunchedEffect(authState.step) {
        Log.d(TAG, "Step: ${authState.step}, route: $currentRouteName")

        when (authState.step) {
            AuthStep.IDLE -> {
                // Not signed in — if not already on welcome/splash, go there
            }

            AuthStep.AUTHENTICATING, AuthStep.CHECKING_PROFILE -> {
                // Show loading — handled by overlay below
            }

            AuthStep.USERNAME_PROMPT -> {
                showUsernameSheet = true
            }

            AuthStep.CREATE_PIN -> {
                showUsernameSheet = false
                navController.navigate(Screen.CreatePin) {
                    popUpTo(0) { inclusive = true }
                }
            }

            AuthStep.BIOMETRIC_SETUP -> {
                // Handled by overlay
            }

            AuthStep.VERIFY_PIN -> {
                showUsernameSheet = false
                navController.navigate(Screen.VerifyPin) {
                    popUpTo(0) { inclusive = true }
                }
            }

            AuthStep.COMPLETE -> {
                showUsernameSheet = false
                navController.navigate(Screen.Home) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // ── Errors ──
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    // ── Google Sign-In ──
    val googleSignInState = composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    Log.d(TAG, "✅ Google success")
                    // Session observer will handle the rest
                }
                is NativeSignInResult.ClosedByUser -> {
                    authViewModel.onGoogleSignInCancelled()
                }
                is NativeSignInResult.Error -> {
                    authViewModel.onGoogleSignInFailed(result.message)
                }
                is NativeSignInResult.NetworkError -> {
                    authViewModel.onGoogleSignInFailed("Network error: ${result.message}")
                }
            }
        },
    )

    // ── Username sheet ──
    if (showUsernameSheet && authState.step == AuthStep.USERNAME_PROMPT) {
        UsernameSheet(
            onDismiss = { /* prevent dismiss */ },
            onSkip = {
                showUsernameSheet = false
                authViewModel.skipUsername()
            },
            onContinue = { username, referral ->
                showUsernameSheet = false
                authViewModel.saveUsername(username, referral)
            },
        )
    }

    // ── Bottom bar ──
    val currentRouteScreen: Screen? = when {
        currentRouteName?.contains(Screen.Home::class.qualifiedName ?: "") == true -> Screen.Home
        currentRouteName?.contains(Screen.Games::class.qualifiedName ?: "") == true -> Screen.Games
        currentRouteName?.contains(Screen.Nft::class.qualifiedName ?: "") == true -> Screen.Nft
        currentRouteName?.contains(Screen.Trade::class.qualifiedName ?: "") == true -> Screen.Trade
        currentRouteName?.contains(Screen.Profile::class.qualifiedName ?: "") == true -> Screen.Profile
        else -> null
    }
    val showBottomBar = currentRouteScreen != null

    Box {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    VexiumBottomBar(
                        currentRoute = currentRouteScreen,
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
                composable<Screen.Splash> {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Welcome) {
                                popUpTo(Screen.Splash) { inclusive = true }
                            }
                        },
                    )
                }

                composable<Screen.SplashAlt> {
                    SplashScreenAlt(
                        onSplashFinished = {
                            navController.navigate(Screen.Welcome) {
                                popUpTo(Screen.SplashAlt) { inclusive = true }
                            }
                        },
                    )
                }

                composable<Screen.Welcome> {
                    WelcomeScreen(
                        onGoogleClick = {
                            authViewModel.onGoogleSignInStarted()
                            googleSignInState.startFlow()
                        },
                        onTelegramClick = { },
                        onEmailClick = { },
                        onGuestClick = {
                            navController.navigate(Screen.Home) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                }

                composable<Screen.CreatePin> {
                    PinScreen(
                        mode = PinMode.CREATE,
                        onPinConfirmed = { pin -> authViewModel.savePin(pin) },
                        onBackClick = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Welcome) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                }

                composable<Screen.VerifyPin> {
                    val activity = context as? FragmentActivity
                    PinScreen(
                        mode = PinMode.VERIFY,
                        onPinConfirmed = { pin -> authViewModel.verifyPin(pin) },
                        onBiometricClick = if (activity != null && BiometricHelper.canAuthenticate(context)) {
                            {
                                BiometricHelper.authenticate(
                                    activity = activity,
                                    onSuccess = { authViewModel.onBiometricSuccess() },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                )
                            }
                        } else null,
                        onBackClick = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Welcome) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                }

                composable<Screen.Home> {
                    HomeScreen(
                        onNavigateToGames = { navController.navigate(Screen.Games) },
                        onNavigateToWallet = { },
                        onLogout = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Welcome) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
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

                composable<Screen.Settings> {
                    GeneralSettingsScreen(
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
        }

        // ════════════════════════════════════
        // LOADING OVERLAYS (on top of everything)
        // ════════════════════════════════════

        // Full-screen loading during auth transitions
        if (authState.isLoading && (
                authState.step == AuthStep.AUTHENTICATING ||
                    authState.step == AuthStep.CHECKING_PROFILE
                )
        ) {
            FullScreenLoading(message = authState.loadingMessage)
        }

        // Modal loading during in-app operations
        if (authState.isLoading && authState.step != AuthStep.AUTHENTICATING && authState.step != AuthStep.CHECKING_PROFILE) {
            ModalLoading(message = authState.loadingMessage)
        }

        // Biometric setup overlay
        if (authState.step == AuthStep.BIOMETRIC_SETUP) {
            val activity = context as? FragmentActivity
            val canBiometric = activity != null && BiometricHelper.canAuthenticate(context)

            if (canBiometric) {
                BiometricSetupScreen(
                    onEnable = {
                        authViewModel.enableBiometric()
                        if (activity != null) {
                            BiometricHelper.authenticate(
                                activity = activity,
                                title = "Enable Biometrics",
                                subtitle = "Verify to enable biometric login",
                                onSuccess = { authViewModel.onBiometricSuccess() },
                                onError = { authViewModel.skipBiometric() },
                                onCancel = { authViewModel.skipBiometric() },
                            )
                        }
                    },
                    onSkip = { authViewModel.skipBiometric() },
                )
            } else {
                // No biometric — skip
                LaunchedEffect(Unit) { authViewModel.skipBiometric() }
            }
        }
    }
}
