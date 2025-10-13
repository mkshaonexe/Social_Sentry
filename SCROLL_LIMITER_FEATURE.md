# Facebook Scroll Limiter Feature

## Overview
A new feature has been added to Social Sentry that helps users take mindful breaks while scrolling on Facebook.

## How It Works

### User Experience
1. **Tracking**: When the user scrolls on Facebook for more than **1 minute** continuously, the app triggers a break popup
2. **Break Popup**: A full-screen overlay appears showing:
   - A countdown timer starting at **30 seconds**
   - Motivational quote to encourage mindfulness
   - The user **cannot** close the popup or use Facebook during these 30 seconds
3. **After Break**: Once the 30 seconds countdown completes, the popup automatically closes and the user can:
   - Continue using Facebook
   - Close the popup manually if it's still visible
4. **Cooldown**: After the popup closes, there's a **1-minute cooldown** period before another popup can be triggered

### Enabling/Disabling the Feature
- Navigate to **Settings** (3-line menu icon in top-left corner)
- Scroll down to find **"Facebook Scroll Limiter"** card
- Use the toggle switch to turn the feature **ON** or **OFF**
- **Note**: Accessibility service must be enabled for this feature to work

## Technical Implementation

### Files Modified/Created

1. **SocialApp.kt** - Added `scrollLimiterEnabled` field to settings
2. **ScrollLimiterOverlayActivity.kt** (NEW) - Full-screen overlay with 30-second countdown
3. **SocialSentryAccessibilityService.kt** - Added scroll tracking and popup triggering logic
4. **SettingsScreen.kt** - Added toggle switch for the feature
5. **SocialSentryViewModel.kt** - Added `updateScrollLimiterEnabled()` function
6. **AndroidManifest.xml** - Registered the overlay activity and added SYSTEM_ALERT_WINDOW permission

### Key Features
- **Smart Tracking**: Only tracks continuous scrolling, resets if user stops scrolling
- **Cooldown System**: 1-minute cooldown after each popup to avoid spam
- **Beautiful UI**: Animated countdown with gradient backgrounds and motivational quotes
- **Accessibility Check**: Feature only works when accessibility service is enabled
- **Non-intrusive**: Only activates when enabled by the user

### Permissions Required
- **SYSTEM_ALERT_WINDOW**: Allows the app to display the overlay on top of Facebook

## Testing the Feature

1. Enable the accessibility service in Settings
2. Enable "Facebook Scroll Limiter" in the app settings
3. Open Facebook app
4. Scroll continuously for 1 minute
5. The break popup should appear
6. Wait 30 seconds for the countdown to finish
7. The popup will close automatically

## Future Enhancements (Suggestions)
- Make the scroll duration threshold configurable (currently fixed at 1 minute)
- Make the break duration configurable (currently fixed at 30 seconds)
- Add different motivational quotes that rotate randomly
- Add statistics showing how many breaks were taken
- Extend to other social media apps (Instagram, TikTok, YouTube)

