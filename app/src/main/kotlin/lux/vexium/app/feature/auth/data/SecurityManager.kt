package lux.vexium.app.feature.auth.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import lux.vexium.app.data.local.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "VexiumSecurity"

@Serializable
data class VerifySessionResponse(
    @SerialName("session_token") val sessionToken: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("device_hash") val deviceHash: String? = null,
    val verified: Boolean = false,
    val error: String? = null,
)

@Serializable
data class ValidateTokenResponse(
    val valid: Boolean = false,
    @SerialName("user_id") val userId: String? = null,
    val error: String? = null,
)

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient,
    private val preferencesManager: PreferencesManager,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get a stable device ID (Android ID).
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }

    /**
     * Full verification: send device_id + user JWT to Edge Function.
     * Returns a short-lived session token.
     * Call on: first open, login, logout.
     */
    suspend fun verifySession(): VerifySessionResponse? {
        return try {
            val accessToken = supabaseClient.auth.currentAccessTokenOrNull() ?: return null
            val deviceId = getDeviceId()

            Log.d(TAG, "Verifying session for device: ${deviceId.take(8)}...")

            val body = buildJsonObject {
                put("device_id", deviceId)
                put("action", "verify")
            }

            val response = supabaseClient.functions.invoke(
                function = "verify-session",
                body = body,
                headers = Headers.build {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                },
            )

            val responseBody = response.body<String>()
            val result = json.decodeFromString<VerifySessionResponse>(responseBody)

            if (result.verified && result.sessionToken != null) {
                preferencesManager.saveDeviceSessionToken(result.sessionToken)
                Log.d(TAG, "Session verified ✅ token: ${result.sessionToken.take(8)}...")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Verify session failed: ${e.message}")
            null
        }
    }

    /**
     * Fast validation: check if existing session token is still valid.
     * Call on: random actions, background checks.
     */
    suspend fun validateToken(): Boolean {
        return try {
            val accessToken = supabaseClient.auth.currentAccessTokenOrNull() ?: return false
            val sessionToken = preferencesManager.getDeviceSessionToken() ?: return false
            val deviceId = getDeviceId()

            val body = buildJsonObject {
                put("session_token", sessionToken)
                put("device_id", deviceId)
            }

            val response = supabaseClient.functions.invoke(
                function = "validate-token",
                body = body,
                headers = Headers.build {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                },
            )

            val responseBody = response.body<String>()
            val result = json.decodeFromString<ValidateTokenResponse>(responseBody)

            Log.d(TAG, "Token validation: ${if (result.valid) "✅" else "❌"}")
            result.valid
        } catch (e: Exception) {
            Log.e(TAG, "Validate token failed: ${e.message}")
            false
        }
    }

    /**
     * Clear device session (on logout).
     */
    suspend fun clearSession() {
        preferencesManager.clearDeviceSessionToken()
    }
}
