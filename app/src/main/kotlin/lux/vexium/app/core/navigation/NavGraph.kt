package lux.vexium.app.core.navigation

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

    var showUsernameSheet by remember { mutableStateOf(false) }
    var showBiometricSetup by remember { mutableStateOf(false) }

    // Helper: check if currently on a specific screen
    val currentRouteName = navBackStackEntry?.destination?.route

    fun isOnScreen(vararg screens: Any): Boolean {
        return screens.any { screen ->
            currentRouteName?.contains(screen::class.qualifiedName ?: "") == true
        }
    }

    // Main tab routes for bottom bar
    val mainTabs = listOf(
        Screen.Home, Screen.Games, Screen.Nft, Screen.Trade, Screen.Profile,
    )

    fun isOnMainTab(): Boolean = mainTabs.any { tab ->
        currentRouteName?.contains(tab::class.qualifiedName ?: "") == true
    }

    // ════════════════════════════════════
    // AUTH FLOW STATE MACHINE
    // ════════════════════════════════════
    LaunchedEffect(
        authState.isSignedIn,
        authState.needsUsername,
        authState.needsPin,
        authState.pinVerified,
        authState.allSetupComplete,
        authState.showBiometricPrompt,
    ) {
        Log.d(TAG, "State: signedIn=${authState.isSignedIn}, needsUser=${authState.needsUsername}, needsPin=${authState.needsPin}, pinVerified=${authState.pinVerified}, allComplete=${authState.allSetupComplete}, route=$currentRouteName")

        if (!authState.isSignedIn) return@LaunchedEffect

        when {
            // Step 1: New user needs username → show bottom sheet
            authState.needsUsername -> {
                Log.d(TAG, "→ Showing username sheet")
                showUsernameSheet = true
            }

            // Step 2: Needs to create PIN
            authState.needsPin && !authState.needsUsername -> {
                Log.d(TAG, "→ Navigate to CreatePin")
                showUsernameSheet = false
                navController.navigate(Screen.CreatePin) {
                    popUpTo(0) { inclusive = true }
                }
            }

            // Step 3: PIN just created, show biometric setup
            authState.showBiometricPrompt && authState.pinVerified -> {
                Log.d(TAG, "→ Showing biometric setup")
                showBiometricSetup = true
            }

            // Step 4: All complete → go home
            authState.allSetupComplete && authState.pinVerified -> {
                Log.d(TAG, "→ Navigate to Home (all complete)")
                showUsernameSheet = false
                showBiometricSetup = false
                if (!isOnMainTab()) {
                    navController.navigate(Screen.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            // Returning user: has PIN, not yet verified
            !authState.needsPin && !authState.needsUsername && !authState.pinVerified -> {
                Log.d(TAG, "→ Navigate to VerifyPin")
                if (!isOnScreen(Screen.VerifyPin)) {
                    navController.navigate(Screen.VerifyPin) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    // ── Signed out → back to welcome ──
    LaunchedEffect(authState.isSignedIn) {
        if (!authState.isSignedIn && !authState.isLoading) {
            val isOnAuthScreen = isOnScreen(Screen.Welcome, Screen.Splash, Screen.SplashAlt)
            if (!isOnAuthScreen && currentRouteName != null) {
                Log.d(TAG, "→ Signed out, back to Welcome")
                navController.navigate(Screen.Welcome) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // ── Errors ──
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
                    Log.d(TAG, "✅ Google success!")
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
            onDismiss = { /* don't dismiss on outside tap */ },
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

    // ── Biometric setup overlay ──
    if (showBiometricSetup) {
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
                            onSuccess = {
                                showBiometricSetup = false
                                authViewModel.onBiometricSuccess()
                            },
                            onError = {
                                showBiometricSetup = false
                                authViewModel.skipBiometric()
                            },
                            onCancel = {
                                showBiometricSetup = false
                                authViewModel.skipBiometric()
                            },
                        )
                    }
                },
                onSkip = {
                    showBiometricSetup = false
                    authViewModel.skipBiometric()
                    // Mark as complete even without biometric
                    authViewModel.onBiometricSuccess()
                },
            )
        } else {
            // No biometric hardware — skip
            showBiometricSetup = false
            authViewModel.skipBiometric()
            authViewModel.onBiometricSuccess()
        }
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

            composable<Screen.SplashAlt> {
                SplashScreenAlt(
                    onSplashFinished = {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(Screen.SplashAlt) { inclusive = true }
                        }
                    },
                )
            }

            // ── Welcome ──
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
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            // ── Create PIN ──
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

            // ── Verify PIN ──
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
