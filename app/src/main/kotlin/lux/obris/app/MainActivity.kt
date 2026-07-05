package lux.obris.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

private const val TAG = "ObrisMain"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var composeAuth: ComposeAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d(TAG, "onCreate START")
            installSplashScreen()
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()

            Log.d(TAG, "Setting content...")

            setContent {
                val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

                ObrisTheme(themeMode = themeMode) {
                    ObrisNavHost(
                        settingsViewModel = settingsViewModel,
                        composeAuth = composeAuth,
                    )
                }
            }

            Log.d(TAG, "onCreate DONE")
        } catch (e: Exception) {
            Log.e(TAG, "CRASH in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
