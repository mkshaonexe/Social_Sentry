package com.example.socialsentry.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SocialApp(
    val name: String,
    val packageName: String,
    val isBlocked: Boolean = false,
    val blockTimeStart: Int = 0, // minutes from midnight
    val blockTimeEnd: Int = 1439, // minutes from midnight (23:59)
    val features: List<BlockableFeature> = emptyList()
)

@Serializable
data class BlockableFeature(
    val name: String,
    val isEnabled: Boolean = true,
    val startTime: Int = 0,
    val endTime: Int = 1439
)

@Serializable
data class SocialSentrySettings(
    val instagram: SocialApp = SocialApp(
        name = "Instagram",
        packageName = "com.instagram.android",
        isBlocked = true,
        features = listOf(
            BlockableFeature(
                name = "Reels",
                isEnabled = true
            ),
            BlockableFeature(
                name = "Stories",
                isEnabled = false
            ),
            BlockableFeature(
                name = "Explore",
                isEnabled = false
            )
        )
    ),
    val youtube: SocialApp = SocialApp(
        name = "YouTube",
        packageName = "com.google.android.youtube",
        isBlocked = true,
        features = listOf(
            BlockableFeature(
                name = "Shorts",
                isEnabled = true
            )
        )
    ),
    val tiktok: SocialApp = SocialApp(
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        isBlocked = true,
        features = emptyList() // Block entire app
    ),
    val facebook: SocialApp = SocialApp(
        name = "Facebook",
        packageName = "com.facebook.katana",
        isBlocked = true,
        features = listOf(
            BlockableFeature(
                name = "Reels",
                isEnabled = true
            ),
            BlockableFeature(
                name = "Stories",
                isEnabled = true
            ),
            BlockableFeature(
                name = "Watch",
                isEnabled = false
            )
        )
    ),
    // Daily temporary unblock configuration (minutes)
    val dailyTemporaryUnblockMinutes: Int = 10,
    // Remaining allowance for the current day in milliseconds
    val remainingTemporaryUnblockMs: Long = 10L * 60_000L,
    // Maximum duration allowed per temporary unblock session in milliseconds
    val maxTemporaryUnblockSessionMs: Long = 10L * 60_000L,
    // Whether a temporary unblock session is currently active
    val isTemporaryUnblockActive: Boolean = false,
    // Epoch millis when the current temporary unblock session started (if active)
    val temporaryUnblockSessionStartEpochMs: Long? = null,
    // ISO-8601 local date (yyyy-MM-dd) when the allowance was last reset
    val lastAllowanceResetLocalDate: String? = null,
    // Scroll limiter feature - shows popup after 1 min of scrolling
    // Individual toggles for each app
    val scrollLimiterYoutubeEnabled: Boolean = false,
    val scrollLimiterFacebookEnabled: Boolean = false,
    val scrollLimiterInstagramEnabled: Boolean = false,
    // Legacy field kept for backwards compatibility
    @Deprecated("Use individual app scroll limiter flags")
    val scrollLimiterEnabled: Boolean = false
)

