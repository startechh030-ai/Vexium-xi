package lux.vexium.app.feature.auth.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import lux.vexium.app.data.local.PreferencesManager
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ProfileDto(
    val id: String,
    val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("referral_code") val referralCode: String? = null,
    @SerialName("referred_by") val referredBy: String? = null,
    @SerialName("is_email_verified") val isEmailVerified: Boolean = false,
    @SerialName("quick_login_pin_hash") val quickLoginPinHash: String? = null,
)

@Serializable
data class ProfileUpdate(
    val username: String? = null,
    @SerialName("referred_by") val referredBy: String? = null,
    @SerialName("quick_login_pin_hash") val quickLoginPinHash: String? = null,
)

@Singleton
class AuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val preferencesManager: PreferencesManager,
) {
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    val isLoggedIn: Flow<Boolean> = auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    fun currentUser(): UserInfo? = auth.currentUserOrNull()

    suspend fun signOut() {
        auth.signOut()
    }

    // ── Profile ──

    suspend fun getProfile(userId: String): ProfileDto? {
        return try {
            postgrest.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<ProfileDto>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(userId: String, update: ProfileUpdate) {
        postgrest.from("profiles")
            .update(update) { filter { eq("id", userId) } }
    }

    // ── Referral lookup ──

    suspend fun findUserByReferralCode(code: String): ProfileDto? {
        return try {
            postgrest.from("profiles")
                .select { filter { eq("referral_code", code) } }
                .decodeSingleOrNull<ProfileDto>()
        } catch (e: Exception) {
            null
        }
    }

    // ── PIN ──

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun savePin(userId: String, pin: String) {
        val hash = hashPin(pin)
        // Save to both Supabase AND local storage
        updateProfile(userId, ProfileUpdate(quickLoginPinHash = hash))
        preferencesManager.savePinHashLocally(hash)
    }

    /**
     * Verify PIN — tries local first (works offline), falls back to Supabase.
     */
    suspend fun verifyPin(userId: String, pin: String): Boolean {
        val hash = hashPin(pin)

        // Try local first (instant, works offline)
        val localHash = preferencesManager.getLocalPinHash()
        if (localHash != null) {
            return localHash == hash
        }

        // Fallback to Supabase (online only)
        return try {
            val profile = getProfile(userId) ?: return false
            val match = profile.quickLoginPinHash == hash
            // Cache it locally for next time
            if (match && profile.quickLoginPinHash != null) {
                preferencesManager.savePinHashLocally(profile.quickLoginPinHash)
            }
            match
        } catch (e: Exception) {
            false
        }
    }
}
