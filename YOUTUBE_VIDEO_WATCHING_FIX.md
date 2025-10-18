# YouTube Video Watching Fix - Scroll Limiter

## Problem
When users were watching YouTube videos (especially in landscape/rotated mode), the scroll limiter was incorrectly detecting video watching as "scrolling" and triggering the 5-minute limit. This created false positives where users couldn't watch a video for more than 5 minutes without getting the break popup.

## Root Cause
The scroll limiter was tracking **all interaction events** in YouTube, including:
- Video player controls and interactions
- Fullscreen/landscape video viewing
- Seeking through video timeline
- Actual feed scrolling

This meant any activity in YouTube counted toward the 5-minute limit, even when just watching a video.

## Solution Implemented

### 1. Added Video Player Detection Function
Created `isUserWatchingYouTubeVideo()` function that:
- Checks the accessibility node hierarchy for video player UI elements
- Looks for specific YouTube video player indicators:
  - `com.google.android.youtube.player`
  - `player_view`
  - `player_fragment_container`
  - `watch_player`
  - `video_surface_view`
  - `player_controls_container`
  - `youtube_controls`
  - `exo_player`
- Recursively searches through the UI hierarchy to detect if a video player is active

### 2. Updated Scroll Tracking Logic
Modified `trackScrollLimiter()` to:
- Check if user is watching a video before counting any scroll/interaction events
- If video player is detected, **skip tracking** and return early
- Only count actual feed scrolling toward the 5-minute limit

### 3. Key Code Changes

**In `trackScrollLimiter()` function (line ~633):**
```kotlin
// Special handling for YouTube: Don't track if user is watching a video
if (appName == "YouTube" && isUserWatchingYouTubeVideo()) {
    Log.v(TAG, "YouTube scroll limiter: User is watching a video, not tracking")
    return
}
```

**New function `isUserWatchingYouTubeVideo()` (line ~587):**
- Searches accessibility hierarchy for video player elements
- Returns `true` if video player is detected (user watching video)
- Returns `false` if in feed/home screen (user scrolling)

## Expected Behavior After Fix

### ✅ What WILL be tracked:
- Scrolling through YouTube home feed (Shorts, recommended videos)
- Browsing through search results
- Scrolling through comments section (not in video player)

### ❌ What will NOT be tracked:
- Watching a video in portrait mode
- Watching a video in landscape/fullscreen mode
- Interacting with video player controls (play, pause, seek)
- Watching videos in any orientation

## Testing Instructions

1. Enable YouTube scroll limiter in settings
2. Open YouTube and start watching a video
3. Watch for more than 5 minutes
4. **Expected**: No break popup should appear
5. Exit the video and scroll through home feed for 5 minutes
6. **Expected**: Break popup should appear after 5 minutes of feed scrolling

## Technical Notes

- The fix uses accessibility service's `rootInActiveWindow` to inspect UI hierarchy
- Video player detection is done on each interaction event (lightweight check)
- Errors in detection are caught and logged to prevent service crashes
- The function properly recycles accessibility nodes to prevent memory leaks

## Files Modified

1. `app/src/main/java/com/example/socialsentry/service/SocialSentryAccessibilityService.kt`
   - Added `isUserWatchingYouTubeVideo()` function
   - Updated `trackScrollLimiter()` to check for video watching before tracking

## Impact

- **Positive**: Users can now watch long-form YouTube videos without interruption
- **Positive**: Scroll limiter only triggers for actual feed scrolling (intended behavior)
- **No negative impact**: Feed scrolling still tracked accurately
- **Performance**: Minimal overhead from video player detection check

