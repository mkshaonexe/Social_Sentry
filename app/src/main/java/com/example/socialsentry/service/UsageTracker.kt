package com.example.socialsentry.service

import android.os.Handler
import android.os.Looper
import com.example.socialsentry.domain.SocialSentryNotificationManager

class UsageTracker(
    private val notificationManager: SocialSentryNotificationManager,
    private val packageName: String,
    private val timeLimitMinutes: Int
) {
    private var startTime: Long = 0
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    private val notificationRunnable = object : Runnable {
        override fun run() {
            sendNotification()
            handler.postDelayed(this, (timeLimitMinutes * 60 * 1000).toLong())
        }
    }

    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            isRunning = true
            handler.postDelayed(notificationRunnable, (timeLimitMinutes * 60 * 1000).toLong())
        }
    }

    fun stop() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(notificationRunnable)
        }
    }

    private fun sendNotification() {
        val appName = when (packageName) {
            "com.instagram.android" -> "Instagram"
            "com.facebook.katana" -> "Facebook"
            "com.instagram.barcelona" -> "Threads"
            else -> "the app"
        }
        notificationManager.showUsageReminderNotification(appName)
    }
}
