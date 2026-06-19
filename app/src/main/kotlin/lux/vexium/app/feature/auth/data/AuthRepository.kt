package lux.vexium.app.feature.auth.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ProfileDto(
    val id: String,
    val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("referral_code") val referralCode: String? = null,
    @SerialName("is_email_verified") val isEmailVerified: Boolean = false,
)

@Singleton
class AuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
) {
    /**
     * Observe session status changes.
     */
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    /**
     * Check if user is currently logged in.
     */
    val isLoggedIn: Flow<Boolean> = auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    /**
     * Get current user info (or null).
     */
    fun currentUser(): UserInfo? = auth.currentUserOrNull()

    /**
     * Sign in with email + password.
     */
    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Sign up with email + password.
     */
    suspend fun signUpWithEmail(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Sign out.
     */
    suspend fun signOut() {
        auth.signOut()
    }

    /**
     * Get user profile from the profiles table.
     */
    suspend fun getProfile(userId: String): ProfileDto? {
        return try {
            postgrest.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<ProfileDto>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Upsert profile (create or update).
     */
    suspend fun upsertProfile(profile: ProfileDto) {
        postgrest.from("profiles").upsert(profile)
    }
}
