# How to Debug Scroll Limiter with Android Studio Logcat

## What is Logcat?

Logcat is Android Studio's built-in tool that shows real-time logs from your app. It helps you see what's happening inside your app while it's running.

## Step-by-Step Guide to View Logs

### 1. Open Logcat in Android Studio

1. **Run your app** on the emulator (if not already running)
2. At the bottom of Android Studio, look for tabs
3. Click on the **"Logcat"** tab
   - If you don't see it, go to: `View` → `Tool Windows` → `Logcat`

### 2. Filter Logs to Show Only SocialSentry

The Logcat shows logs from ALL apps, so we need to filter it:

1. In the Logcat window, find the **filter dropdown** (usually says "Show only selected application")
2. Make sure your app package `com.example.socialsentry` is selected
3. In the **search box** at the top of Logcat, type: `SocialSentry`
   - This will show only logs from our app

### 3. Clear Old Logs (Optional but Recommended)

- Click the **🗑️ trash icon** in the Logcat toolbar to clear old logs
- This makes it easier to see new logs when you test

### 4. Set Log Level

- In the log level dropdown, select **"Debug"** or **"Verbose"**
- This ensures you see all our debug messages

## What to Look For When Testing Scroll Limiter

### Step 1: Enable the Scroll Limiter
1. Open Social Sentry app
2. Go to Settings (☰ menu)
3. Turn ON "Facebook Scroll Limiter"
4. Make sure accessibility service is enabled

### Step 2: Open Facebook and Start Scrolling
Open Facebook app and start scrolling through your feed

### Step 3: Watch for These Logs

While scrolling, you should see logs like this in Logcat:

#### When You Start Scrolling:
```
D/SocialSentry: Facebook detected, scroll limiter enabled, tracking...
D/SocialSentry: Facebook scroll limiter: ⏱️ Started tracking scroll time
```

#### Every Few Seconds While Scrolling:
```
V/SocialSentry: Facebook scroll limiter: Duration = 5s
V/SocialSentry: Facebook scroll limiter: Duration = 10s
V/SocialSentry: Facebook scroll limiter: Duration = 15s
```

#### After 20 Seconds:
```
D/SocialSentry: Facebook scroll limiter: 20s elapsed
```

#### After 40 Seconds:
```
D/SocialSentry: Facebook scroll limiter: 40s elapsed
```

#### After 60 Seconds (1 Minute):
```
D/SocialSentry: Facebook scroll limiter: 🚨 1 minute threshold reached (60s), showing overlay
```

### Step 4: What If You Don't See These Logs?

#### Scenario A: No Logs at All
If you see **NO logs** from SocialSentry while scrolling:
- The accessibility service might not be running
- Check in Settings → Accessibility → SocialSentry is ON

#### Scenario B: See "Facebook detected" but Nothing Else
If you only see:
```
D/SocialSentry: Facebook detected, scroll limiter enabled, tracking...
```
But NO "Started tracking scroll time" message:
- The app is not detecting scroll events
- This means Facebook's UI might have changed or scroll events aren't firing

#### Scenario C: Timer Resets Before 60 Seconds
If you see "Reset due to inactivity":
```
D/SocialSentry: Facebook scroll limiter: Reset due to inactivity
```
- You paused scrolling for more than 3 seconds
- The timer resets to prevent false triggers
- Keep scrolling continuously to reach 60 seconds

#### Scenario D: See "In cooldown period"
If you see:
```
V/SocialSentry: Facebook scroll limiter: In cooldown period
```
- You recently saw the popup (within last 35 seconds)
- Wait 35 seconds after the popup closes before scrolling again

## You Should Also See Toast Messages

Besides Logcat, you should see **toast notifications** (small popup messages) on your phone:

1. **When you start scrolling:**
   - "⏱️ Scroll timer started (1 min limit)"

2. **At 20 seconds:**
   - "⏱️ 20s / 60s scrolled"

3. **At 40 seconds:**
   - "⏱️ 40s / 60s scrolled"

4. **At 60 seconds:**
   - The full-screen 30-second break popup should appear

## Troubleshooting Tips

### Tip 1: Keep Scrolling Continuously
- Don't pause for more than 3 seconds while scrolling
- Scroll at a steady pace

### Tip 2: Scroll in the Main Facebook Feed
- Make sure you're in the Facebook home feed (news feed)
- Not in a specific post, not in comments, not in Reels

### Tip 3: Check Package Name
Look for this log to confirm Facebook package:
```
D/SocialSentry: Package: com.facebook.katana
```

If you see a different package name, that's not the main Facebook app.

### Tip 4: Reinstall the APK
If you made changes to the code:
1. Rebuild: `.\gradlew.bat assembleDebug`
2. Find APK at: `app\build\outputs\apk\debug\app-debug.apk`
3. Drag and drop to emulator to install
4. Force stop the app and restart it

## Quick Test Checklist

- [ ] App is running on emulator
- [ ] Logcat is open and filtered to "SocialSentry"
- [ ] Scroll limiter is enabled in Settings
- [ ] Accessibility service is enabled
- [ ] Facebook app is installed on emulator
- [ ] Scrolling in Facebook main feed
- [ ] Watching Logcat for debug messages
- [ ] Looking for toast notifications on screen

## Common Issues

### Issue 1: "Facebook detected but scroll limiter is disabled"
**Fix:** Go to Settings and turn ON the "Facebook Scroll Limiter" toggle

### Issue 2: No logs appear at all
**Fix:** 
1. Make sure the app is running
2. Check Logcat filter is set correctly
3. Try changing log level to "Verbose"

### Issue 3: Timer keeps resetting
**Fix:** Scroll more continuously without long pauses

### Issue 4: Events aren't TYPE_VIEW_SCROLLED
**Fix:** This is a Facebook app issue - we might need to adjust our event detection logic

## Need More Help?

If you still don't see the expected logs, please:
1. Clear Logcat (🗑️ icon)
2. Start scrolling in Facebook
3. Copy ALL the logs from Logcat (Ctrl+A, Ctrl+C)
4. Share the logs so we can see what events are actually being detected

