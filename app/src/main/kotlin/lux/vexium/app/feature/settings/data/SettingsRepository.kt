package lux.vexium.app.feature.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lux.vexium.app.core.theme.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vexium_settings"
)

/**
 * Manages all user settings using DataStore.
 * Any screen/feature can read settings as Flows and write changes.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.settingsDataStore

    // ═══════════════════════════════════
    //  KEYS
    // ═══════════════════════════════════
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        // Add more setting keys here as needed:
        // val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        // val LANGUAGE = stringPreferencesKey("language")
    }

    // ═══════════════════════════════════
    //  THEME
    // ═══════════════════════════════════
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }

    // ═══════════════════════════════════
    //  CLEAR ALL
    // ═══════════════════════════════════
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
