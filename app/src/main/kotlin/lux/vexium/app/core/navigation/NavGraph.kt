package lux.vexium.app.core.navigation

import android.app.Activity
import android.util.Log
import android.widget.Toast
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

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Track if username sheet should show
    var showUsernameSheet by remember { mutableStateOf(false) }

    // ── React to auth state changes ──
    LaunchedEffect(authState.isSignedIn, authState.needsUsername, authState.needsPin, authState.allSetupComplete) {
        val currentRoute = navController.currentDestination?.route

        if (authState.isSignedIn) {
            val isOnWelcome = currentRoute == Screen.Welcome::class.qualifiedName

            when {
                // New user needs username
                authState.needsUsername && isOnWelcome -> {
                    showUsernameSheet = true
                }
                // Needs PIN creation
                authState.needsPin && !authState.needsUsername -> {
                    showUsernameSheet = false
                    if (currentRoute != Screen.CreatePin::class.qualifiedName) {
                        navController.navigate(Screen.CreatePin) {
                            popUpTo(Screen.Welcome) { inclusive = true }
                        }
                    }
                }
                // Has PIN, needs verification (returning user)
                !authState.needsPin && !authState.pinVerified && !authState.allSetupComplete -> {
                    if (currentRoute != Screen.VerifyPin::class.qualifiedName &&
                        currentRoute != Screen.CreatePin::class.qualifiedName) {
                        navController.navigate(Screen.VerifyPin) {
                            popUpTo(Screen.Welcome) { inclusive = true }
                        }
                    }
                }
                // All setup complete → Home
                authState.allSetupComplete -> {
                    showUsernameSheet = false
                    val notOnMainTab = currentRoute != Screen.Home::class.qualifiedName &&
                        currentRoute != Screen.Games::class.qualifiedName &&
                        currentRoute != Screen.Nft::class.qualifiedName &&
                        currentRoute != Screen.Trade::class.qualifiedName &&
                        currentRoute != Screen.Profile::class.qualifiedName

                    if (notOnMainTab) {
                        navController.navigate(Screen.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    // ── Show errors ──
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Log.e(TAG, "Auth error: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    // ── Google Sign-In ──
    val googleSignInState = composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            Log.d(TAG, "Google result: $result")
            when (result) {
                is NativeSignInResult.Success -> {
                    Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
                }
                is NativeSignInResult.ClosedByUser -> {
                    Toast.makeText(context, "Sign in cancelled", Toast.LENGTH_SHORT).show()
                }
                is NativeSignInResult.Error -> {
                    Toast.makeText(context, "Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is NativeSignInResult.NetworkError -> {
                    Toast.makeText(context, "Network error: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        },
    )

    // ── Username bottom sheet ──
    if (showUsernameSheet) {
        UsernameSheet(
            onDismiss = { showUsernameSheet = false },
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
                        Log.d(TAG, "Starting Google Sign-In...")
                        googleSignInState.startFlow()
                    },
                    onTelegramClick = { },
                    onEmailClick = { },
                    onGuestClick = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Welcome) { inclusive = true }
                        }
                    },
                )
            }

            composable<Screen.CreatePin> {
                PinScreen(
                    mode = PinMode.CREATE,
                    onPinConfirmed = { pin ->
                        authViewModel.savePin(pin)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                )

                // After PIN saved, show biometric setup
                if (authState.showBiometricPrompt && !authState.needsPin) {
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
                            onSkip = {
                                authViewModel.skipBiometric()
                            },
                        )
                    }
                }
            }

            composable<Screen.VerifyPin> {
                val activity = context as? FragmentActivity

                PinScreen(
                    mode = PinMode.VERIFY,
                    onPinConfirmed = { pin ->
                        authViewModel.verifyPin(pin)
                    },
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

            // ── Main Tabs ──
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
}
