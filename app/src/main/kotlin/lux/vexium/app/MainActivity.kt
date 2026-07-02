package lux.vexium.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.compose.auth.ComposeAuth
import lux.vexium.app.core.navigation.VexiumNavHost
import lux.vexium.app.core.theme.VexiumTheme
import lux.vexium.app.feature.settings.presentation.SettingsViewModel
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var composeAuth: ComposeAuth

    // Store the pending Telegram code from deep link
    var pendingTelegramCode: String? = null
        private set

    fun consumeTelegramCode(): String? {
        val code = pendingTelegramCode
        pendingTelegramCode = null
        return code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if opened via deep link
        handleDeepLink(intent)

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

            VexiumTheme(themeMode = themeMode) {
                VexiumNavHost(
                    settingsViewModel = settingsViewModel,
                    composeAuth = composeAuth,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        Log.d(TAG, "Deep link: $uri")

        // vexium://auth/telegram?code=XXXXXXXX
        if (uri.scheme == "vexium" && uri.host == "auth" && uri.path == "/telegram") {
            val code = uri.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                Log.d(TAG, "Telegram code received: ${code.take(4)}...")
                pendingTelegramCode = code
            }
        }
    }
}
