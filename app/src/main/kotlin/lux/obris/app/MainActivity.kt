package lux.obris.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.compose.auth.ComposeAuth
import lux.obris.app.core.navigation.ObrisNavHost
import lux.obris.app.core.theme.ObrisTheme
import lux.obris.app.feature.settings.presentation.SettingsViewModel
import javax.inject.Inject

/**
 * Main entry point for Obris.
 * Uses AppCompatActivity for biometric support.
 * Landscape only.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var composeAuth: ComposeAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

            ObrisTheme(themeMode = themeMode) {
                ObrisNavHost(
                    settingsViewModel = settingsViewModel,
                    composeAuth = composeAuth,
                )
            }
        }
    }
}
