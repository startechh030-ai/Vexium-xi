package lux.vexium.app.feature.auth.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }

    suspend fun verifySession(): VerifySessionResponse? {
        return try {
            val accessToken = supabaseClient.auth.currentAccessTokenOrNull()
            if (accessToken == null) {
                Log.w(TAG, "No access token — skipping verify")
                return null
            }

            val deviceId = getDeviceId()
            Log.d(TAG, "Verifying session for device: ${deviceId.take(8)}...")

            val body = buildJsonObject {
                put("device_id", deviceId)
                put("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
                put("os_version", "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                put("app_version", "1.0.0")
                put("action", "verify")
            }

            val result: VerifySessionResponse = supabaseClient.functions.invoke(
                function = "verify-session",
                body = body,
            )

            Log.d(TAG, "Verify result: verified=${result.verified}")

            if (result.verified && result.sessionToken != null) {
                preferencesManager.saveDeviceSessionToken(result.sessionToken)
                Log.d(TAG, "Session verified ✅")
            } else {
                Log.w(TAG, "Verify failed: ${result.error}")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Verify session error: ${e.message}", e)
            null
        }
    }

    suspend fun validateToken(): Boolean {
        return try {
            val sessionToken = preferencesManager.getDeviceSessionToken() ?: return false
            val deviceId = getDeviceId()

            val body = buildJsonObject {
                put("session_token", sessionToken)
                put("device_id", deviceId)
            }

            val result: ValidateTokenResponse = supabaseClient.functions.invoke(
                function = "validate-token",
                body = body,
            )

            Log.d(TAG, "Validate: ${if (result.valid) "✅" else "❌"}")
            result.valid
        } catch (e: Exception) {
            Log.e(TAG, "Validate error: ${e.message}")
            false
        }
    }

    suspend fun clearSession() {
        preferencesManager.clearDeviceSessionToken()
    }
}
