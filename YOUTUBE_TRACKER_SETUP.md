# YouTube Tracker - Quick Setup Guide

## ✅ Build Status: **SUCCESSFUL**

The YouTube tracker has been successfully implemented and compiled! Here's how to use it:

---

## 📋 What You Need

1. **Enable Accessibility Service** (if not already enabled)
   - Go to: Settings → Accessibility → Social Sentry
   - Turn ON the accessibility service
   - ⚠️ This is required for YouTube tracking to work

2. **Install the App**
   - The APK is ready at: `app/build/outputs/apk/debug/app-debug.apk`
   - Install it on your Android device

---

## 🎯 How to Use

### Step 1: View YouTube Stats
1. Open the **Social Sentry** app
2. **Swipe left** to open the Game Dashboard (or navigate to it from the menu)
3. **Scroll down** to see the **"YouTube Usage Tracker"** section

### Step 2: Let It Track
1. Open **YouTube app** on your device
2. Watch any video (the tracker runs automatically in the background)
3. The tracker will:
   - Detect the video title
   - Identify the channel
   - Auto-categorize the video (Study, Entertainment, Gaming, etc.)
   - Track watch time

### Step 3: Check Your Stats
Go back to the **Game Dashboard** to see:
- **Total Watch Time** for today (in hours and minutes)
- **Pie Chart** showing category breakdown
- **Category List** with percentages and time spent
- **Top Channels** you watched
- **Recent Videos** you watched (last 5)

### Step 4: Correct Categories (Optional)
If a video was categorized incorrectly:
1. In the **"Recent Videos"** section, **tap on the video**
2. A dialog will appear with all category options
3. **Select the correct category**
4. The app will:
   - Update that video's category
   - Remember your correction for similar videos in the future

---

## 🎨 What the Dashboard Shows

### YouTube Usage Tracker Section

```
┌─────────────────────────────────────┐
│ 🔴 YouTube Usage Tracker            │
├─────────────────────────────────────┤
│                                     │
│  Today's Watch Time                 │
│        2h 45m                       │
│                                     │
├─────────────────────────────────────┤
│  Category Breakdown                 │
│                                     │
│     [Animated Pie Chart]            │
│                                     │
│  📚 Study        120m    44%        │
│  🎬 Entertainment 75m    27%        │
│  🎮 Gaming        30m    11%        │
│  💼 Productive    45m    16%        │
│  ❓ Other          5m     2%        │
│                                     │
├─────────────────────────────────────┤
│  Top Channels                       │
│                                     │
│  MK Shaon         60m               │
│  Physics Wallah   45m               │
│  Gaming Channel   30m               │
│                                     │
├─────────────────────────────────────┤
│  Recent Videos    [Tap to correct]  │
│                                     │
│  📚 HSC Physics Ch 3 Lecture        │
│      Physics Wallah • 25m           │
│                                     │
│  🎬 Top 10 Funny Moments            │
│      Comedy Central • 12m           │
│                                     │
└─────────────────────────────────────┘
```

---

## 📊 Category Examples

### 📚 Study (Green)
Videos detected as educational content:
- "HSC ICT Chapter 4 Lecture"
- "Bangla 1st Paper Class"
- "Math Problem Solution"
- "Physics Tutorial"

### 🎬 Entertainment (Pink)
Fun and entertainment content:
- "Top 10 Funny Fails"
- "Music Video Official"
- "Movie Trailer"
- "Comedy Sketch"

### 🎮 Gaming (Purple)
Gaming-related videos:
- "PUBG Gameplay"
- "Minecraft Let's Play"
- "Valorant Walkthrough"

### 💼 Productive (Blue)
Productive but not study:
- "How to Code in Python"
- "Productivity Tips"
- "Career Advice"
- "Tech Review"

### ❓ Other (Gray)
Uncategorized or ambiguous

---

## 🔍 Auto-Categorization Keywords

The tracker uses **70+ smart keywords** to categorize videos:

**Study Keywords:**
- lecture, class, tutorial, chapter, exam
- HSC, SSC, math, physics, chemistry, biology
- bangla, history, solution, viva, problem

**Entertainment Keywords:**
- music, song, movie, trailer, comedy
- funny, vlog, reaction, prank, meme

**Gaming Keywords:**
- gameplay, walkthrough, let's play
- pubg, freefire, valorant, minecraft

**Productive Keywords:**
- coding, how to, tips, career
- productivity, programming, developer

---

## 💡 Tips for Best Results

### 1. **Keep Accessibility Service Enabled**
   - The tracker only works when the service is running
   - Check: Settings → Accessibility → Social Sentry → ON

### 2. **Watch Videos Normally**
   - No special action needed
   - Just use YouTube as usual
   - The tracker runs silently in the background

### 3. **Correct Misclassified Videos**
   - If a study video is marked as entertainment, tap it and correct it
   - The app learns from your corrections
   - Future similar videos will be categorized correctly

### 4. **Check Stats Daily**
   - Stats reset every day at midnight
   - Review your progress each evening
   - See how much study vs entertainment time

### 5. **Use It for Self-Improvement**
   - Set daily study goals
   - Reduce entertainment time
   - Track progress over time

---

## 🛠️ Troubleshooting

### "No YouTube usage data yet"
**Cause:** You haven't watched any videos yet, or tracking isn't working.

**Solutions:**
1. Make sure **Accessibility Service is enabled**
2. Open YouTube and watch a video for at least 5 seconds
3. Go back to the dashboard and check

### Videos not being tracked
**Cause:** Accessibility service might be disabled or YouTube UI changed.

**Solutions:**
1. Check if accessibility service is running
2. Try restarting the app
3. Re-enable the accessibility service

### Wrong category assigned
**Solution:** Simply tap the video in "Recent Videos" and correct it!

### Stats showing 0 minutes
**Cause:** Videos watched for less than 5 seconds are filtered out.

**Solution:** This is intentional to avoid counting accidental taps.

---

## 🔒 Privacy & Security

✅ **All data stays on your device**
- No internet connection required
- No data sent to any server
- 100% local storage using DataStore

✅ **Only YouTube is tracked**
- Other apps are not monitored for usage
- Only active when YouTube is in foreground

✅ **User control**
- You can disable tracking anytime
- Data can be reset
- Full transparency

---

## 📈 Example Daily Workflow

**Morning:**
- Watch 2 HSC lectures (30 min each) → **Study: 60m**

**Afternoon:**
- Watch music videos (20 min) → **Entertainment: 20m**
- Watch coding tutorial (15 min) → **Productive: 15m**

**Evening:**
- Watch gaming highlights (25 min) → **Gaming: 25m**
- Watch comedy sketch (10 min) → **Entertainment: 10m**

**End of Day Dashboard:**
```
Total Watch Time: 2h 10m

Category Breakdown:
📚 Study:        60m  (46%)
🎬 Entertainment: 30m  (23%)
💼 Productive:   15m  (12%)
🎮 Gaming:       25m  (19%)
```

**Insights:**
- Great job! Study content was your top category
- Consider reducing gaming/entertainment time tomorrow
- Keep up the productive content!

---

## 🎓 Best Practices for Students

1. **Morning Review**: Check yesterday's stats every morning
2. **Set Goals**: Aim for 60%+ study content daily
3. **Limit Distractions**: Keep entertainment under 30 minutes
4. **Correct Early**: Fix categories as you go for better accuracy
5. **Track Channels**: See which channels help you study most

---

## 🚀 Next Steps

1. Install the app and enable accessibility
2. Watch some YouTube videos
3. Check your stats on the Game Dashboard
4. Correct any miscategorized videos
5. Use insights to improve your study habits!

---

**Enjoy tracking your YouTube usage and becoming more productive! 📊✨**

