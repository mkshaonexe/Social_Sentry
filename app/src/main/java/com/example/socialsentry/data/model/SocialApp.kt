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
        features = listOf(
            BlockableFeature(
                name = "Reels",
                isEnabled = false
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
        features = listOf(
            BlockableFeature(
                name = "Shorts",
                isEnabled = false
            )
        )
    ),
    val tiktok: SocialApp = SocialApp(
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        features = emptyList() // Block entire app
    ),
    val facebook: SocialApp = SocialApp(
        name = "Facebook",
        packageName = "com.facebook.katana",
        features = listOf(
            BlockableFeature(
                name = "Reels",
                isEnabled = false
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
    )
)

