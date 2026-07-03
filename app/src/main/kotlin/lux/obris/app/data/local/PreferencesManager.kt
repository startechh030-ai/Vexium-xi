package lux.obris.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "obris_preferences"
)

/**
 * Local preferences for Obris — session state, user info cache.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
    }

    // ── User ID ──
    val userId: Flow<String?> = dataStore.data.map { it[Keys.USER_ID] }

    suspend fun saveUserId(id: String) {
        dataStore.edit { it[Keys.USER_ID] = id }
    }

    // ── Display name ──
    suspend fun saveUserDisplayName(name: String) {
        dataStore.edit { it[Keys.USER_DISPLAY_NAME] = name }
    }

    suspend fun getUserDisplayName(): String? {
        return dataStore.data.map { it[Keys.USER_DISPLAY_NAME] }.first()
    }

    // ── Clear all (on logout) ──
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
