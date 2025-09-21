package com.example.socialsentry.service

import android.content.Context
import android.util.Log
import com.example.socialsentry.data.datastore.SocialSentryDataStore
import com.example.socialsentry.data.model.WorkoutSession
import com.example.socialsentry.data.model.WorkoutSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

class WorkoutSessionManager(
    private val context: Context
) : KoinComponent {
    
    private val dataStore: SocialSentryDataStore by inject()
    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _currentSession = MutableStateFlow<WorkoutSession?>(null)
    val currentSession: StateFlow<WorkoutSession?> = _currentSession.asStateFlow()
    
    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()
    
    private val _pushUpCount = MutableStateFlow(0)
    val pushUpCount: StateFlow<Int> = _pushUpCount.asStateFlow()
    
    private val _minutesEarned = MutableStateFlow(0)
    val minutesEarned: StateFlow<Int> = _minutesEarned.asStateFlow()
    
    companion object {
        private const val TAG = "WorkoutSessionManager"
    }
    
    fun startWorkoutSession() {
        if (_isWorkoutActive.value) {
            Log.w(TAG, "Workout session already active")
            return
        }
        
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        
        val newSession = WorkoutSession(
            id = sessionId,
            startTime = startTime,
            endTime = 0L,
            pushUpsCompleted = 0,
            minutesEarned = 0,
            isCompleted = false
        )
        
        _currentSession.value = newSession
        _isWorkoutActive.value = true
        _pushUpCount.value = 0
        _minutesEarned.value = 0
        
        Log.d(TAG, "Workout session started: $sessionId")
    }
    
    fun onPushUpDetected() {
        if (!_isWorkoutActive.value) return
        
        val currentCount = _pushUpCount.value + 1
        _pushUpCount.value = currentCount
        
        // Calculate minutes earned (1 push-up = 1 minute by default)
        sessionScope.launch {
            val settings = dataStore.getWorkoutSettings()
            val minutesPerPushUp = settings.minutesPerPushUp
            val newMinutesEarned = currentCount * minutesPerPushUp
            
            _minutesEarned.value = newMinutesEarned
            
            // Update current session
            _currentSession.value = _currentSession.value?.copy(
                pushUpsCompleted = currentCount,
                minutesEarned = newMinutesEarned
            )
            
            Log.d(TAG, "Push-up detected! Count: $currentCount, Minutes earned: $newMinutesEarned")
        }
    }
    
    fun endWorkoutSession() {
        if (!_isWorkoutActive.value) {
            Log.w(TAG, "No active workout session to end")
            return
        }
        
        val currentSession = _currentSession.value ?: return
        val endTime = System.currentTimeMillis()
        
        val completedSession = currentSession.copy(
            endTime = endTime,
            isCompleted = true
        )
        
        sessionScope.launch {
            try {
                // Save workout session
                dataStore.saveWorkoutSession(completedSession)
                
                // Update workout settings with earned minutes
                val currentSettings = dataStore.getWorkoutSettings()
                val updatedSettings = currentSettings.copy(
                    totalMinutesEarned = currentSettings.totalMinutesEarned + completedSession.minutesEarned,
                    totalPushUpsCompleted = currentSettings.totalPushUpsCompleted + completedSession.pushUpsCompleted,
                    lastWorkoutDate = getCurrentDate(),
                    currentSessionMinutes = completedSession.minutesEarned
                )
                
                dataStore.updateWorkoutSettings(updatedSettings)
                
                Log.d(TAG, "Workout session completed: ${completedSession.pushUpsCompleted} push-ups, ${completedSession.minutesEarned} minutes earned")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save workout session", e)
            }
        }
        
        // Reset session state
        _currentSession.value = null
        _isWorkoutActive.value = false
        _pushUpCount.value = 0
        _minutesEarned.value = 0
        
        Log.d(TAG, "Workout session ended")
    }
    
    fun pauseWorkoutSession() {
        if (!_isWorkoutActive.value) return
        
        _isWorkoutActive.value = false
        Log.d(TAG, "Workout session paused")
    }
    
    fun resumeWorkoutSession() {
        if (_currentSession.value == null) return
        
        _isWorkoutActive.value = true
        Log.d(TAG, "Workout session resumed")
    }
    
    fun resetCurrentSession() {
        _currentSession.value = null
        _isWorkoutActive.value = false
        _pushUpCount.value = 0
        _minutesEarned.value = 0
        Log.d(TAG, "Current session reset")
    }
    
    suspend fun getWorkoutSettings(): WorkoutSettings {
        return dataStore.getWorkoutSettings()
    }
    
    suspend fun updateWorkoutSettings(settings: WorkoutSettings) {
        dataStore.updateWorkoutSettings(settings)
    }
    
    suspend fun getTodayMinutesEarned(): Int {
        val settings = dataStore.getWorkoutSettings()
        return if (settings.lastWorkoutDate == getCurrentDate()) {
            settings.currentSessionMinutes
        } else {
            0
        }
    }
    
    suspend fun canEarnMoreMinutes(): Boolean {
        val settings = dataStore.getWorkoutSettings()
        val todayMinutes = getTodayMinutesEarned()
        
        return if (settings.dailyLimitMinutes > 0) {
            todayMinutes < settings.dailyLimitMinutes
        } else {
            true // No daily limit
        }
    }
    
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    fun destroy() {
        sessionScope.cancel()
    }
}
