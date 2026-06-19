package lux.vexium.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.compose.auth.ComposeAuth
import lux.vexium.app.core.navigation.VexiumNavHost
import lux.vexium.app.core.theme.VexiumTheme
import lux.vexium.app.feature.settings.presentation.SettingsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var composeAuth: ComposeAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}
