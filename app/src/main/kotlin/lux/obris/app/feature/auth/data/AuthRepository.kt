package lux.obris.app.feature.auth.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Supabase auth — sign in/out + session observation.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
) {
    /** Observe session changes */
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    /** Current user or null */
    fun currentUser(): UserInfo? = auth.currentUserOrNull()

    /** Sign out and clear session */
    suspend fun signOut() {
        auth.signOut()
    }
}
