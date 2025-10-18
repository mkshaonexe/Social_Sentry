# Scroll Limiter Multi-App Feature - Implementation Summary

## Overview
Successfully implemented individual scroll limiter toggles for YouTube, Facebook, and Instagram with a clean, expandable settings menu similar to Android's permission manager interface.

## Changes Made

### 1. Data Model Updates (`SocialApp.kt`)
- Added three new boolean fields to `SocialSentrySettings`:
  - `scrollLimiterYoutubeEnabled: Boolean = false`
  - `scrollLimiterFacebookEnabled: Boolean = false`
  - `scrollLimiterInstagramEnabled: Boolean = false`
- Deprecated the old `scrollLimiterEnabled` field for backwards compatibility

### 2. Accessibility Service Updates (`SocialSentryAccessibilityService.kt`)

#### New Features:
- **Multi-app scroll tracking**: Extended scroll limiter to support YouTube and Instagram in addition to Facebook
- **Per-app tracking variables**: Each app now has its own set of tracking variables:
  - Scroll start time
  - Last scroll time
  - Scroll limiter shown flag
  - Last trigger time
  - Last progress notification second
  - Mandatory break end time

#### Key Methods:
- `trackScrollLimiter(event, appName)`: Generic scroll tracking function for all apps
- Helper methods for managing per-app state:
  - `setAppScrollStartTime()`
  - `setAppLastScrollTime()`
  - `setAppScrollLimiterShown()`
  - `setAppLastTriggerTime()`
  - `setAppLastProgressSecond()`
  - `setAppBreakEndTime()`
  - `resetAppScrollTracking()`

#### Functionality:
- **Facebook**: Tracks user scrolling/interaction for 2 minutes, shows progress toasts at 20s, 40s, 60s, 80s, 100s
- **YouTube**: Tracks user scrolling/interaction for 5 minutes, shows progress toasts at 60s, 120s, 180s, 240s
- **Other apps**: Tracks user scrolling/interaction for 1 minute, shows progress toasts at 20s and 40s
- Triggers mandatory 30-second break after reaching the time threshold
- Each app has independent tracking and break timers
- Respects individual app settings from the settings screen

### 3. ViewModel Updates (`SocialSentryViewModel.kt`)
Added three new methods for individual scroll limiter toggles:
- `updateScrollLimiterYoutubeEnabled(enabled: Boolean)`
- `updateScrollLimiterFacebookEnabled(enabled: Boolean)`
- `updateScrollLimiterInstagramEnabled(enabled: Boolean)`

### 4. Settings Screen UI (`SettingsScreen.kt`)

#### New UI Components:
- **Expandable Scroll Limiter Section**: Similar to "Reels Block" section
  - Header with title, description, and expand/collapse arrow
  - Click to expand/collapse the section
  - Shows accessibility warning if service not enabled

- **Individual App Toggles** (when expanded):
  - 📺 **YouTube** toggle with app icon
  - 📘 **Facebook** toggle with app icon
  - 📷 **Instagram** toggle with app icon
  - Each toggle shows:
    - App name with emoji icon
    - Description: "Limit scrolling time"
    - Green/gray switch (enabled/disabled)
    - Disabled state when accessibility service is off

#### UI Design:
- Clean Material Design 3 cards
- Consistent spacing and layout
- Professional color scheme (green for enabled, gray for disabled)
- Disabled state when accessibility service not enabled
- Follows the same design pattern as "Reels Block" section

### 5. Overlay Activity Updates (`ScrollLimiterOverlayActivity.kt`)
- Added app name parameter to show which app triggered the break
- Updated overlay message to include app name: "You've been scrolling {AppName} for over 1 minute"
- Receives app name from intent extras

## How It Works

### User Flow:
1. **Enable Accessibility Service** (required for all features)
2. **Open Settings Screen** → Tap hamburger menu (☰) icon
3. **Expand "Scroll Limiter"** section → Click on the header
4. **Toggle Individual Apps**:
   - Enable YouTube scroll limiter ✓
   - Enable Facebook scroll limiter ✓
   - Enable Instagram scroll limiter ✓

### Runtime Behavior:
1. **Start Tracking**: When user opens a monitored app, tracking starts automatically
2. **Progress Notifications**: Shows toast at 20s and 40s of usage
3. **Mandatory Break**: After 1 minute, shows full-screen overlay with 30-second countdown
4. **App-Specific Breaks**: Each app has its own independent break timer
5. **Reset on Inactivity**: Tracking resets after 8 seconds of no interaction

## Technical Details

### Event Tracking:
Monitors multiple event types for accurate tracking:
- `TYPE_VIEW_SCROLLED` (4096)
- `TYPE_WINDOW_STATE_CHANGED` (32)
- `TYPE_VIEW_CLICKED` (1)
- `TYPE_WINDOW_CONTENT_CHANGED` (2048)

### Configuration:
- **Scroll Duration**: 1 minute (60,000 ms)
- **Break Duration**: 30 seconds (30,000 ms)
- **Inactivity Reset**: 8 seconds (8,000 ms)
- **Cooldown Period**: 10 seconds (10,000 ms)

### Package Names:
- YouTube: `com.google.android.youtube`
- Facebook: `com.facebook.katana`
- Instagram: `com.instagram.android`

## Build Status
✅ Build successful with no errors
⚠️ Minor deprecation warnings (non-critical, do not affect functionality)

## Testing Recommendations
1. Enable accessibility service
2. Enable scroll limiter for each app individually
3. Test each app separately to verify independent tracking
4. Verify 1-minute countdown and 30-second break overlay
5. Test overlay shows correct app name
6. Verify tracking resets after inactivity
7. Test multiple apps in succession

## Future Enhancements (Optional)
- Configurable scroll duration threshold (allow users to set custom time limits)
- Configurable break duration
- Statistics/analytics dashboard showing usage time per app
- Weekly/monthly usage reports
- Custom break messages per app
- Sound/vibration alerts at milestones

