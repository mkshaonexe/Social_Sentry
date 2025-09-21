package com.example.socialsentry.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutSession(
    val id: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val pushUpsCompleted: Int = 0,
    val minutesEarned: Int = 0,
    val isCompleted: Boolean = false
)

@Serializable
data class WorkoutSettings(
    val isEnabled: Boolean = false,
    val dailyLimitMinutes: Int = 0, // 0 means unlimited
    val minutesPerPushUp: Int = 1, // 1 push-up = 1 minute
    val totalMinutesEarned: Int = 0,
    val totalPushUpsCompleted: Int = 0,
    val lastWorkoutDate: String = "", // YYYY-MM-DD format
    val currentSessionMinutes: Int = 0 // Minutes earned in current session
)

@Serializable
data class SocialSentrySettings(
    val instagram: SocialApp = SocialApp(
        name = "Instagram",
        packageName = "com.instagram.android",
        isBlocked = false,
        blockTimeStart = 0,
        blockTimeEnd = 1440,
        features = listOf(
            BlockableFeature("Reels", true, 0, 1440),
            BlockableFeature("Stories", true, 0, 1440),
            BlockableFeature("Explore", true, 0, 1440)
        )
    ),
    val youtube: SocialApp = SocialApp(
        name = "YouTube",
        packageName = "com.google.android.youtube",
        isBlocked = false,
        blockTimeStart = 0,
        blockTimeEnd = 1440,
        features = listOf(
            BlockableFeature("Shorts", true, 0, 1440)
        )
    ),
    val tiktok: SocialApp = SocialApp(
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        isBlocked = false,
        blockTimeStart = 0,
        blockTimeEnd = 1440,
        features = emptyList()
    ),
    val facebook: SocialApp = SocialApp(
        name = "Facebook",
        packageName = "com.facebook.katana",
        isBlocked = false,
        blockTimeStart = 0,
        blockTimeEnd = 1440,
        features = listOf(
            BlockableFeature("Reels", true, 0, 1440),
            BlockableFeature("Stories", true, 0, 1440),
            BlockableFeature("Watch", true, 0, 1440)
        )
    ),
    val workoutSettings: WorkoutSettings = WorkoutSettings()
)
