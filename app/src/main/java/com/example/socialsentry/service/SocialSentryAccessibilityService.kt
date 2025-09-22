package com.example.socialsentry.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.socialsentry.data.datastore.SocialSentryDataStore
import com.example.socialsentry.data.model.SocialSentrySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.TimeZone

class SocialSentryAccessibilityService : AccessibilityService(), KoinComponent {

    private val dataStore: SocialSentryDataStore by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var settings = SocialSentrySettings()

    // Anti-spam mechanism
    private var lastBlockTime = 0L
    private var lastFacebookBlockTime = 0L
    private val blockDebounceMs = 300L
    private val facebookDebounceMs = 1200L
    private var lastReelsCheckTime = 0L
    private val reelsCheckIntervalMs = 600L

    companion object {
        private const val TAG = "SocialSentry"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "SocialSentry Accessibility Service connected")
        serviceScope.launch {
            dataStore.settingsFlow.collect { latestSettings ->
                settings = latestSettings
                Log.d(TAG, "Settings updated: ${settings}")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return

        // Filter relevant events to avoid excessive processing
        val eventType = event.eventType
        val isRelevantEvent = when (eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> true
            else -> false
        }

        if (!isRelevantEvent) return

        Log.v(TAG, "Processing event from $packageName, type: $eventType")

        val currentMinute = getCurrentMinuteOfDay()
        
        // If user has an active temporary unblock session, allow content and skip blocking
        if (settings.isTemporaryUnblockActive) {
            return
        }
        
        when (packageName) {
            "com.instagram.android" -> handleInstagram(root, currentMinute)
            "com.google.android.youtube" -> handleYoutube(root, currentMinute)
            "com.zhiliaoapp.musically" -> handleTikTok(currentMinute)
            "com.facebook.katana" -> handleFacebook(root, event, currentMinute)
        }
    }

    private fun handleInstagram(root: AccessibilityNodeInfo, currentMinute: Int) {
        val app = settings.instagram
        if (!app.isBlocked || !isWithinTimeRange(app.blockTimeStart, app.blockTimeEnd, currentMinute)) {
            return
        }

        val reelsFeature = app.features.find { it.name == "Reels" }
        if (reelsFeature?.isEnabled == true && isWithinTimeRange(reelsFeature.startTime, reelsFeature.endTime, currentMinute)) {
            blockInstagram(root)
        }
    }
    
    private fun blockInstagram(root: AccessibilityNodeInfo) {
        // V9-style Instagram blocking - look for Reels container
        val reelView = root.findAccessibilityNodeInfosByViewId(
            "com.instagram.android:id/clips_swipe_refresh_container"
        ).firstOrNull()

        if (reelView != null) {
            Log.d(TAG, "Instagram Reels detected - redirecting to feed")
            val feedTab = root.findAccessibilityNodeInfosByViewId(
                "com.instagram.android:id/feed_tab"
            ).firstOrNull()
            exitTheDoom(feedTab)
        }
    }

    private fun handleYoutube(root: AccessibilityNodeInfo, currentMinute: Int) {
        val app = settings.youtube
        if (!app.isBlocked || !isWithinTimeRange(app.blockTimeStart, app.blockTimeEnd, currentMinute)) {
            return
        }

        val shortsFeature = app.features.find { it.name == "Shorts" }
        if (shortsFeature?.isEnabled == true && isWithinTimeRange(shortsFeature.startTime, shortsFeature.endTime, currentMinute)) {
            blockYouTube(root)
        }
    }
    
    private fun blockYouTube(root: AccessibilityNodeInfo) {
        // V9-style YouTube blocking - look for Shorts container
        root.findAccessibilityNodeInfosByViewId(
            "com.google.android.youtube:id/reel_watch_fragment_root"
        ).firstOrNull() ?: return

        Log.d(TAG, "YouTube Shorts detected - redirecting to home")
        val pivotBar = root.findAccessibilityNodeInfosByViewId(
            "com.google.android.youtube:id/pivot_bar"
        ).firstOrNull()
        val homeTab = pivotBar?.getChild(0)?.getChild(0)

        exitTheDoom(homeTab) {
            homeTab?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    private fun handleTikTok(currentMinute: Int) {
        val app = settings.tiktok
        if (!app.isBlocked || !isWithinTimeRange(app.blockTimeStart, app.blockTimeEnd, currentMinute)) {
            return
        }

        blockTikTok()
    }
    
    private fun blockTikTok() {
        // V9-style TikTok blocking - always go to home screen
        Log.d(TAG, "TikTok blocked - going to home screen")
        exitTheDoom(null) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun handleFacebook(root: AccessibilityNodeInfo, event: AccessibilityEvent?, currentMinute: Int) {
        val app = settings.facebook
        if (!app.isBlocked || !isWithinTimeRange(app.blockTimeStart, app.blockTimeEnd, currentMinute)) {
            return
        }

        // Enhanced Facebook detection with periodic checking
        val now = System.currentTimeMillis()
        val shouldPeriodicCheck = (now - lastReelsCheckTime) > reelsCheckIntervalMs
        
        if (shouldPeriodicCheck) {
            lastReelsCheckTime = now
            Log.v(TAG, "Performing periodic Facebook content check")
        }

        if (detectFacebookBlockableContent(root, event)) {
            if (now - lastFacebookBlockTime < facebookDebounceMs) {
                Log.v(TAG, "Facebook blocking debounced")
                return
            }
            
            Log.d(TAG, "Facebook blocked content detected - redirecting")
            lastFacebookBlockTime = now
            redirectToFacebookHome(root)
        }
    }


    // Facebook Detection Methods  
    private fun detectFacebookBlockableContent(root: AccessibilityNodeInfo, event: AccessibilityEvent?): Boolean {
        val app = settings.facebook
        val reelsEnabled = app.features.find { it.name == "Reels" }?.isEnabled ?: false
        val storiesEnabled = app.features.find { it.name == "Stories" }?.isEnabled ?: false
        val watchEnabled = app.features.find { it.name == "Watch" }?.isEnabled ?: false

        if (!reelsEnabled && !storiesEnabled && !watchEnabled) return false

        // Check if user clicked on blocked content
        event?.source?.let { clickedNode ->
            if (isBlockedFacebookElement(clickedNode)) {
                Log.d(TAG, "User clicked on blocked Facebook element")
                return true
            }
        }

        // Check if currently in blocked content view
        return isInFacebookBlockedView(root)
    }

    private fun isBlockedFacebookElement(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val description = node.contentDescription?.toString()?.lowercase() ?: ""
        val className = node.className?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        
        val combined = "$text $description $className $viewId"
        
        val blockedKeywords = listOf(
            "reels", "stories", "watch", "video", "reel", "story",
            "for you", "explore", "short video", "vertical video",
            "like", "comment", "share", "save", "follow"
        )
        
        return blockedKeywords.any { keyword ->
            combined.contains(keyword)
        }
    }

    private fun isInFacebookBlockedView(root: AccessibilityNodeInfo): Boolean {
        // Method 1: Check for blocked view IDs
        val blockedViewIds = listOf(
            "com.facebook.katana:id/reels_viewer_container",
            "com.facebook.katana:id/stories_viewer_container",
            "com.facebook.katana:id/video_player_container",
            "com.facebook.katana:id/watch_fragment"
        )
        
        for (viewId in blockedViewIds) {
            if (root.findAccessibilityNodeInfosByViewId(viewId).isNotEmpty()) {
                Log.d(TAG, "Facebook blocked content detected via view ID: $viewId")
                return true
            }
        }

        // Method 2: Check for Reels navigation tabs
        val hasForYou = findTextInNode(root, listOf("for you", "For You", "FOR YOU"))
        val hasExplore = findTextInNode(root, listOf("explore", "Explore", "EXPLORE"))
        
        if (hasForYou && hasExplore) {
            Log.d(TAG, "Facebook Reels detected via navigation tabs")
            return true
        }

        // Method 3: Check for vertical video layout
        return detectVerticalVideoContent(root, "Facebook")
    }

    private fun redirectToFacebookHome(root: AccessibilityNodeInfo) {
        // Try to find navigation bar
        val navBarIds = listOf(
            "com.facebook.katana:id/tab_bar",
            "com.facebook.katana:id/bottom_navigation",
            "com.facebook.katana:id/main_tab_bar"
        )
        
        for (navId in navBarIds) {
            val navBar = root.findAccessibilityNodeInfosByViewId(navId).firstOrNull()
            if (navBar != null && navBar.childCount > 0) {
                val homeTab = navBar.getChild(0) // Usually first tab is home
                if (homeTab != null && homeTab.isClickable) {
                    safePerformAction(homeTab) {
                        homeTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    return
                }
            }
        }
        
        // Fallback to back button
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    // Utility Methods
    private fun detectVerticalVideoContent(root: AccessibilityNodeInfo, platform: String): Boolean {
        return searchForVerticalVideo(root, 0, platform)
    }

    private fun searchForVerticalVideo(node: AccessibilityNodeInfo, depth: Int, platform: String): Boolean {
        if (depth > 4) return false // Limit recursion depth
        
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        
        val isVertical = bounds.height() > bounds.width()
        val isLarge = bounds.width() > 300 && bounds.height() > 500
        
        if (isVertical && isLarge) {
            val className = node.className?.toString()?.lowercase() ?: ""
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            
            val videoKeywords = listOf("video", "surface", "media", "player", "image")
            val combined = "$className $viewId"
            
            if (videoKeywords.any { combined.contains(it) }) {
                Log.d(TAG, "$platform vertical video detected: ${bounds.width()}x${bounds.height()}")
                return true
            }
        }
        
        // Check children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                if (searchForVerticalVideo(child, depth + 1, platform)) {
                    return true
                }
            }
        }
        
        return false
    }

    private fun findTextInNode(root: AccessibilityNodeInfo, searchTexts: List<String>): Boolean {
        return searchTexts.any { searchText ->
            root.findAccessibilityNodeInfosByText(searchText).isNotEmpty()
        }
    }

    private fun safePerformAction(node: AccessibilityNodeInfo?, action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastBlockTime < blockDebounceMs) return
        
        lastBlockTime = now
        try {
            action()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform action", e)
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun getCurrentMinuteOfDay(): Int {
        val now = System.currentTimeMillis()
        val offset = TimeZone.getDefault().getOffset(now)
        return (((now + offset) / 60000) % 1440).toInt()
    }

    private fun isWithinTimeRange(startMinute: Int, endMinute: Int, currentMinute: Int): Boolean {
        return if (startMinute <= endMinute) {
            currentMinute in startMinute..endMinute
        } else {
            // Overnight range
            currentMinute >= startMinute || currentMinute <= endMinute
        }
    }
    
    // V9-style debounced action execution
    private fun exitTheDoom(
        node: AccessibilityNodeInfo?,
        extra: (() -> Unit)? = null
    ) {
        val now = System.currentTimeMillis()
        if (now - lastBlockTime < blockDebounceMs) return
        lastBlockTime = now

        if (node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
        extra?.invoke()
    }

    override fun onInterrupt() {
        Log.d(TAG, "SocialSentry Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "SocialSentry Accessibility Service destroyed")
    }
}
