package lux.vexium.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vexium_preferences"
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val HAS_PIN = booleanPreferencesKey("has_pin")
        val HAS_USERNAME = booleanPreferencesKey("has_username")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LAST_ROUTE = stringPreferencesKey("last_route")
    }

    // ── User ID ──
    val userId: Flow<String?> = dataStore.data.map { it[Keys.USER_ID] }

    suspend fun saveUserId(id: String) {
        dataStore.edit { it[Keys.USER_ID] = id }
    }

    // ── PIN status ──
    val hasPin: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_PIN] ?: false }

    suspend fun setHasPin(value: Boolean) {
        dataStore.edit { it[Keys.HAS_PIN] = value }
    }

    // ── Username status ──
    val hasUsername: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_USERNAME] ?: false }

    suspend fun setHasUsername(value: Boolean) {
        dataStore.edit { it[Keys.HAS_USERNAME] = value }
    }

    // ── Biometric ──
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    suspend fun setBiometricEnabled(value: Boolean) {
        dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = value }
    }

    // ── Last route ──
    val lastRoute: Flow<String?> = dataStore.data.map { it[Keys.LAST_ROUTE] }

    suspend fun saveLastRoute(route: String) {
        dataStore.edit { it[Keys.LAST_ROUTE] = route }
    }

    // ── Clear all (on logout) ──
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
