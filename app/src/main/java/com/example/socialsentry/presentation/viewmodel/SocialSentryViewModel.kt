package com.example.socialsentry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialsentry.data.datastore.SocialSentryDataStore
import com.example.socialsentry.data.model.BlockableFeature
import com.example.socialsentry.data.model.SocialApp
import com.example.socialsentry.data.model.SocialSentrySettings
import com.example.socialsentry.data.model.WorkoutSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SocialSentryViewModel(
    private val dataStore: SocialSentryDataStore
) : ViewModel() {

    val settings: StateFlow<SocialSentrySettings> = dataStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SocialSentrySettings()
        )

    fun updateInstagram(app: SocialApp) {
        viewModelScope.launch {
            dataStore.updateInstagram(app)
        }
    }

    fun updateYoutube(app: SocialApp) {
        viewModelScope.launch {
            dataStore.updateYoutube(app)
        }
    }

    fun updateTiktok(app: SocialApp) {
        viewModelScope.launch {
            dataStore.updateTiktok(app)
        }
    }

    fun updateFacebook(app: SocialApp) {
        viewModelScope.launch {
            dataStore.updateFacebook(app)
        }
    }

    fun toggleAppBlocking(appName: String, isBlocked: Boolean) {
        val currentSettings = settings.value
        viewModelScope.launch {
            when (appName) {
                "Instagram" -> updateInstagram(currentSettings.instagram.copy(isBlocked = isBlocked))
                "YouTube" -> updateYoutube(currentSettings.youtube.copy(isBlocked = isBlocked))
                "TikTok" -> updateTiktok(currentSettings.tiktok.copy(isBlocked = isBlocked))
                "Facebook" -> updateFacebook(currentSettings.facebook.copy(isBlocked = isBlocked))
            }
        }
    }

    fun toggleFeatureBlocking(appName: String, featureName: String, isEnabled: Boolean) {
        val currentSettings = settings.value
        viewModelScope.launch {
            when (appName) {
                "Instagram" -> {
                    val updatedFeatures = currentSettings.instagram.features.map { feature ->
                        if (feature.name == featureName) feature.copy(isEnabled = isEnabled) else feature
                    }
                    updateInstagram(currentSettings.instagram.copy(features = updatedFeatures))
                }
                "YouTube" -> {
                    val updatedFeatures = currentSettings.youtube.features.map { feature ->
                        if (feature.name == featureName) feature.copy(isEnabled = isEnabled) else feature
                    }
                    updateYoutube(currentSettings.youtube.copy(features = updatedFeatures))
                }
                "Facebook" -> {
                    val updatedFeatures = currentSettings.facebook.features.map { feature ->
                        if (feature.name == featureName) feature.copy(isEnabled = isEnabled) else feature
                    }
                    updateFacebook(currentSettings.facebook.copy(features = updatedFeatures))
                }
            }
        }
    }

    fun updateAppTimeRange(appName: String, startMinute: Int, endMinute: Int) {
        val currentSettings = settings.value
        viewModelScope.launch {
            when (appName) {
                "Instagram" -> {
                    updateInstagram(
                        currentSettings.instagram.copy(
                            blockTimeStart = startMinute,
                            blockTimeEnd = endMinute
                        )
                    )
                }
                "YouTube" -> {
                    updateYoutube(
                        currentSettings.youtube.copy(
                            blockTimeStart = startMinute,
                            blockTimeEnd = endMinute
                        )
                    )
                }
                "TikTok" -> {
                    updateTiktok(
                        currentSettings.tiktok.copy(
                            blockTimeStart = startMinute,
                            blockTimeEnd = endMinute
                        )
                    )
                }
                "Facebook" -> {
                    updateFacebook(
                        currentSettings.facebook.copy(
                            blockTimeStart = startMinute,
                            blockTimeEnd = endMinute
                        )
                    )
                }
            }
        }
    }
    
    // Workout-related methods
    fun updateWorkoutSettings(workoutSettings: WorkoutSettings) {
        viewModelScope.launch {
            dataStore.updateWorkoutSettings(workoutSettings)
        }
    }
    
    suspend fun getWorkoutSettings(): WorkoutSettings {
        return dataStore.getWorkoutSettings()
    }
    
    suspend fun getTodayMinutesEarned(): Int {
        return dataStore.getTodayWorkoutSessions().sumOf { it.minutesEarned }
    }
    
    suspend fun canEarnMoreMinutes(): Boolean {
        val settings = getWorkoutSettings()
        val todayMinutes = getTodayMinutesEarned()
        
        return if (settings.dailyLimitMinutes > 0) {
            todayMinutes < settings.dailyLimitMinutes
        } else {
            true // No daily limit
        }
    }
}

