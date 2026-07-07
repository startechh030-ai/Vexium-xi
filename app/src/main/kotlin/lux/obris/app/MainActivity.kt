package lux.obris.app

import android.os.Bundle
import android.util.Log
import android.view.View
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

/**
 * Main entry — landscape, full screen, every pixel used.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var composeAuth: ComposeAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Full screen BEFORE super.onCreate — prevents any system UI flash
        requestFullScreen()

        try {
            installSplashScreen()
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            hideSystemUI()

            setContent {
                val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
                ObrisTheme(themeMode = themeMode) {
                    ObrisNavHost(settingsViewModel = settingsViewModel, composeAuth = composeAuth)
                }
            }
        } catch (e: Exception) {
            Log.e("ObrisMain", "CRASH: ${e.message}", e)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    /** Request full screen via window flags — works before setContent */
    @Suppress("DEPRECATION")
    private fun requestFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    /** Hide system bars using modern API */
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
