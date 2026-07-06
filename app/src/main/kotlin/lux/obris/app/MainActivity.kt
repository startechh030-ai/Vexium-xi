package lux.obris.app

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.compose.auth.ComposeAuth
import lux.obris.app.core.navigation.ObrisNavHost
import lux.obris.app.core.theme.ObrisTheme
import lux.obris.app.feature.settings.presentation.SettingsViewModel
import javax.inject.Inject

private const val TAG = "ObrisMain"

/**
 * Main entry point — landscape, full screen edge-to-edge.
 * Hides status bar, nav bar — every pixel is game space.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var composeAuth: ComposeAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            installSplashScreen()
            super.onCreate(savedInstanceState)

            // ── Full screen: hide everything ──
            enableEdgeToEdge()
            hideSystemUI()

            setContent {
                val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

                ObrisTheme(themeMode = themeMode) {
                    ObrisNavHost(
                        settingsViewModel = settingsViewModel,
                        composeAuth = composeAuth,
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "CRASH: ${e.message}", e)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    /** Hide status bar, navigation bar — true full screen */
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Keep screen on during splash
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
