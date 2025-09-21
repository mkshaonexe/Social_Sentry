package com.example.socialsentry.data.datastore

import android.content.Context
import androidx.datastore.dataStore
import com.example.socialsentry.data.model.SocialApp
import com.example.socialsentry.data.model.SocialSentrySettings
import com.example.socialsentry.data.model.WorkoutSession
import com.example.socialsentry.data.model.WorkoutSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.File

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
    
    suspend fun getCurrentSettings(): SocialSentrySettings {
        return settingsFlow.first()
    }
    
    // Workout-related methods
    suspend fun updateWorkoutSettings(workoutSettings: WorkoutSettings) {
        context.socialSentrySettingsStore.updateData { current ->
            current.copy(workoutSettings = workoutSettings)
        }
    }
    
    suspend fun getWorkoutSettings(): WorkoutSettings {
        return settingsFlow.first().workoutSettings
    }
    
    suspend fun saveWorkoutSession(session: WorkoutSession) {
        val sessionsFile = File(context.filesDir, "workout_sessions.json")
        val existingSessions = if (sessionsFile.exists()) {
            try {
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString<List<WorkoutSession>>(sessionsFile.readText())
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        
        val updatedSessions = existingSessions + session
        val json = Json { ignoreUnknownKeys = true }
        sessionsFile.writeText(json.encodeToString(List.serializer(WorkoutSession.serializer()), updatedSessions))
    }
    
    suspend fun getWorkoutSessions(): List<WorkoutSession> {
        val sessionsFile = File(context.filesDir, "workout_sessions.json")
        return if (sessionsFile.exists()) {
            try {
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString<List<WorkoutSession>>(sessionsFile.readText())
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun getTodayWorkoutSessions(): List<WorkoutSession> {
        val allSessions = getWorkoutSessions()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        
        return allSessions.filter { session ->
            val sessionDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date(session.startTime))
            sessionDate == today
        }
    }
}

