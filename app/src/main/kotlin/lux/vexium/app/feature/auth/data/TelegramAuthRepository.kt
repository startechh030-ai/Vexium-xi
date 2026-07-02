package lux.vexium.app.feature.auth.data

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TelegramAuth"

@Serializable
data class TelegramVerifyResponse(
    val success: Boolean = false,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("is_new_user") val isNewUser: Boolean = false,
    @SerialName("telegram_id") val telegramId: Long? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val token: String? = null,
    val type: String? = null,
    @SerialName("redirect_to") val redirectTo: String? = null,
    @SerialName("auth_method") val authMethod: String? = null,
    val error: String? = null,
)

@Singleton
class TelegramAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Verify a Telegram auth code with the Edge Function.
     * Returns user info + session data.
     */
    suspend fun verifyCode(code: String): TelegramVerifyResponse {
        Log.d(TAG, "Verifying code: ${code.take(4)}...")

        val body = buildJsonObject {
            put("code", code)
        }

        val httpResponse = supabaseClient.functions.invoke(
            function = "telegram-verify",
            body = body,
        )

        val responseText = httpResponse.bodyAsText()
        Log.d(TAG, "Verify response: $responseText")

        return json.decodeFromString<TelegramVerifyResponse>(responseText)
    }

    /**
     * Complete sign-in using the magic link token from telegram-verify.
     */
    suspend fun completeSignIn(token: String, type: String) {
        Log.d(TAG, "Completing sign-in with token type: $type")

        // Use the token to verify OTP and create a session
        supabaseClient.auth.verifyEmailOtp(
            type = when (type) {
                "magiclink" -> io.github.jan.supabase.auth.OtpType.Email.MAGIC_LINK
                else -> io.github.jan.supabase.auth.OtpType.Email.MAGIC_LINK
            },
            tokenHash = token,
        )

        Log.d(TAG, "Sign-in complete ✅")
    }
}
