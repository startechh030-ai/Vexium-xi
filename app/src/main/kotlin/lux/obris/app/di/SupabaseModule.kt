package lux.obris.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import lux.obris.app.core.common.Constants
import javax.inject.Singleton

/**
 * Supabase DI module — provides client, auth, postgrest, compose auth.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_ANON_KEY,
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "lux.obris.app"
                host = "auth-callback"
            }
            install(Postgrest)
            install(ComposeAuth) {
                googleNativeLogin(serverClientId = Constants.GOOGLE_WEB_CLIENT_ID)
            }
        }
    }

    @Provides @Singleton
    fun provideAuth(client: SupabaseClient): Auth = client.auth

    @Provides @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides @Singleton
    fun provideComposeAuth(client: SupabaseClient): ComposeAuth = client.composeAuth
}
