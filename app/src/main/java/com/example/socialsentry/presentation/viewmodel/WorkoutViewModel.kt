package com.example.socialsentry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialsentry.data.datastore.SocialSentryDataStore
import com.example.socialsentry.data.model.WorkoutSettings
import com.example.socialsentry.service.WorkoutSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WorkoutUiState(
    val isWorkoutActive: Boolean = false,
    val pushUpCount: Int = 0,
    val minutesEarned: Int = 0,
    val minutesPerPushUp: Int = 1,
    val dailyLimitMinutes: Int = 0,
    val totalMinutesEarned: Int = 0,
    val totalPushUpsCompleted: Int = 0,
    val canEarnMoreMinutes: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

class WorkoutViewModel(
    private val dataStore: SocialSentryDataStore
) : ViewModel(), KoinComponent {
    
    private val sessionManager: WorkoutSessionManager by inject()
    
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    
    init {
        loadWorkoutSettings()
        observeSessionManager()
    }
    
    private fun loadWorkoutSettings() {
        viewModelScope.launch {
            try {
                val settings = dataStore.getWorkoutSettings()
                _uiState.value = _uiState.value.copy(
                    minutesPerPushUp = settings.minutesPerPushUp,
                    dailyLimitMinutes = settings.dailyLimitMinutes,
                    totalMinutesEarned = settings.totalMinutesEarned,
                    totalPushUpsCompleted = settings.totalPushUpsCompleted,
                    canEarnMoreMinutes = sessionManager.canEarnMoreMinutes()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load workout settings: ${e.message}"
                )
            }
        }
    }
    
    private fun observeSessionManager() {
        viewModelScope.launch {
            combine(
                sessionManager.isWorkoutActive,
                sessionManager.pushUpCount,
                sessionManager.minutesEarned
            ) { isActive, pushUps, minutes ->
                _uiState.value.copy(
                    isWorkoutActive = isActive,
                    pushUpCount = pushUps,
                    minutesEarned = minutes
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    fun startWorkout() {
        viewModelScope.launch {
            try {
                if (!sessionManager.canEarnMoreMinutes()) {
                    _uiState.value = _uiState.value.copy(
                        error = "Daily limit reached! You cannot earn more minutes today."
                    )
                    return@launch
                }
                
                sessionManager.startWorkoutSession()
                _uiState.value = _uiState.value.copy(
                    error = null,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start workout: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun pauseWorkout() {
        sessionManager.pauseWorkoutSession()
    }
    
    fun resumeWorkout() {
        sessionManager.resumeWorkoutSession()
    }
    
    fun endWorkout() {
        viewModelScope.launch {
            try {
                sessionManager.endWorkoutSession()
                loadWorkoutSettings() // Refresh settings after workout
                _uiState.value = _uiState.value.copy(
                    error = null,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to end workout: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun resetSession() {
        sessionManager.resetCurrentSession()
    }
    
    fun onPushUpDetected() {
        sessionManager.onPushUpDetected()
    }
    
    fun updateWorkoutSettings(settings: WorkoutSettings) {
        viewModelScope.launch {
            try {
                dataStore.updateWorkoutSettings(settings)
                loadWorkoutSettings()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update settings: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        sessionManager.destroy()
    }
}
