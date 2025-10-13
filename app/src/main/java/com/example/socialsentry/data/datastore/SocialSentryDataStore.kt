package com.example.socialsentry.data.datastore

import android.content.Context
import androidx.datastore.dataStore
import com.example.socialsentry.data.model.SocialApp
import com.example.socialsentry.data.model.GameDashboard
import com.example.socialsentry.data.model.SocialSentrySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

val Context.socialSentrySettingsStore by dataStore(
    "social_sentry_settings.json", 
    SocialSentrySettingsSerializer
)

class SocialSentryDataStore(private val context: Context) {
    
    val settingsFlow: Flow<SocialSentrySettings> = context.socialSentrySettingsStore.data
    
    suspend fun updateSettings(settings: SocialSentrySettings) {
        context.socialSentrySettingsStore.updateData { settings }
    }
    
    suspend fun updateInstagram(app: SocialApp) {
        context.socialSentrySettingsStore.updateData { current ->
            current.copy(instagram = app)
        }
    }
    
    suspend fun updateYoutube(app: SocialApp) {
        context.socialSentrySettingsStore.updateData { current ->
            current.copy(youtube = app)
        }
    }
    
    suspend fun updateTiktok(app: SocialApp) {
        context.socialSentrySettingsStore.updateData { current ->
            current.copy(tiktok = app)
        }
    }
    
    suspend fun updateFacebook(app: SocialApp) {
        context.socialSentrySettingsStore.updateData { current ->
            current.copy(facebook = app)
        }
    }
    
    suspend fun updateGameDashboard(dashboard: GameDashboard) {
        context.socialSentrySettingsStore.updateData { current ->
            current.copy(gameDashboard = dashboard)
        }
    }
    
    suspend fun getCurrentSettings(): SocialSentrySettings {
        return settingsFlow.first()
    }
}

