package lux.vexium.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lux.vexium.app.core.common.Constants
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFERENCES_NAME
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    // ── Auth Token ──
    private val authTokenKey = stringPreferencesKey(Constants.AUTH_TOKEN_KEY)

    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[authTokenKey]
    }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[authTokenKey] = token
        }
    }

    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(authTokenKey)
        }
    }

    // ── Wallet Address ──
    private val walletAddressKey = stringPreferencesKey(Constants.WALLET_ADDRESS_KEY)

    val walletAddress: Flow<String?> = dataStore.data.map { preferences ->
        preferences[walletAddressKey]
    }

    suspend fun saveWalletAddress(address: String) {
        dataStore.edit { preferences ->
            preferences[walletAddressKey] = address
        }
    }

    // ── Clear All ──
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
