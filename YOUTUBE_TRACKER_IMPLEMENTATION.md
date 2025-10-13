# YouTube Usage Tracker Implementation Summary

## Overview
A comprehensive YouTube usage tracking system has been implemented using AccessibilityService. The tracker automatically categorizes videos and displays detailed statistics on the Game Dashboard screen.

## ✅ Features Implemented

### 1. **Data Models** (`YouTubeTracking.kt`)
- **YouTubeSession**: Stores individual video watching sessions with:
  - Video title and channel name
  - Start/end times and duration
  - Auto-categorization (Study, Entertainment, Gaming, Productive, Other)
  - Confidence score for categorization
  
- **YouTubeStats**: Aggregated statistics including:
  - Total watch time
  - Category breakdown (time spent per category)
  - Top channels by watch time
  
- **CategoryKeyword**: Smart keyword matching system with:
  - 70+ pre-defined keywords for accurate categorization
  - Support for Bangla/English educational content
  - Channel-based categorization

### 2. **Data Storage** (`YouTubeTrackingDataStore.kt`)
- Persistent local storage using DataStore
- Daily stats with automatic reset
- Session management (add, update, end sessions)
- User correction storage for learning

### 3. **Automatic Tracking** (`SocialSentryAccessibilityService.kt`)
- **Real-time video detection**:
  - Extracts video title from YouTube UI
  - Detects channel name
  - Tracks watch time accurately
  
- **Smart session management**:
  - Automatically starts/ends sessions
  - Minimum 5-second duration filter (avoids counting accidental taps)
  - Handles app switching gracefully
  
- **Categorization on-the-fly**:
  - Instant categorization using keyword matching
  - Learns from user corrections

### 4. **Game Dashboard Integration** (`GameDashboardScreen.kt`)
- **YouTube Stats Section** with:
  - Total daily watch time (hours + minutes)
  - Beautiful animated pie chart showing category breakdown
  - Percentage breakdown by category with color-coded indicators
  - Top 5 channels by watch time
  - Recent videos list (last 5 videos)

### 5. **Category Correction UI**
- **Recent Videos List**:
  - Shows last 5 watched videos
  - Displays title, channel, duration, and current category
  - Tap any video to correct its category
  
- **Correction Dialog**:
  - Visual category selection with icons
  - Updates session category instantly
  - Saves correction to improve future categorization

## 📊 Category System

### Categories with Smart Keywords

#### 📚 **Study** (Green - #4CAF50)
Keywords: lecture, class, tutorial, chapter, exam, HSC, SSC, math, physics, chemistry, biology, history, bangla, solution, viva, problem, question, lesson, learn, education, university, college

#### 🎬 **Entertainment** (Pink - #E91E63)
Keywords: music, song, mv, official video, movie, trailer, comedy, funny, vlog, reaction, prank, meme, tiktok, shorts, dance, entertainment

#### 🎮 **Gaming** (Purple - #9C27B0)
Keywords: gameplay, walkthrough, let's play, game, overwatch, pubg, freefire, mobile legends, valorant, minecraft, fortnite, gta, cod, call of duty, gaming

#### 💼 **Productive** (Blue - #2196F3)
Keywords: coding, how to, tips, career, productivity, programming, developer, tech, review, guide, documentary, news

#### ❓ **Other** (Gray - #757575)
Uncategorized or ambiguous content

## 🎨 UI Features

### Dashboard Display (Portrait Mode)
1. Player Section (profile, stats)
2. Quick Stats with Radar Chart
3. **NEW: YouTube Usage Tracker** with:
   - Total watch time card
   - Category pie chart
   - Category breakdown list
   - Top channels
   - Recent videos (tappable for corrections)

### Dashboard Display (Landscape Mode)
1. Quote Section
2. Statistics with Radar Chart
3. System Section
4. **NEW: YouTube Usage Tracker**

### Visual Design
- Dark theme matching the app's aesthetic
- YouTube red accent color (#FF0000)
- Smooth animations and transitions
- Color-coded categories for easy identification
- Interactive elements with visual feedback

## 🔄 Data Flow

```
1. User opens YouTube video
   ↓
2. AccessibilityService detects title & channel
   ↓
3. Auto-categorize using keyword matching
   ↓
4. Start tracking session (timestamp)
   ↓
5. User switches video or leaves app
   ↓
6. End session & calculate duration
   ↓
7. Save to local DataStore
   ↓
8. Display stats on Game Dashboard
   ↓
9. User can correct category (optional)
   ↓
10. Learn from correction for future videos
```

## 🎯 Key Highlights

### Accuracy
- **Multi-method title extraction**: Uses multiple ViewID combinations and fallback text search
- **Minimum duration filter**: Only counts videos watched for ≥5 seconds
- **Session debouncing**: Avoids counting quick app switches

### Privacy
- **100% local**: All data stored on device using DataStore
- **No internet required**: Categorization happens on-device
- **No data sharing**: Zero external tracking

### Learning System
- User corrections are saved
- Future videos with same title auto-categorized correctly
- Keyword database can be expanded through corrections

### Performance
- Minimal battery impact (only processes relevant accessibility events)
- Efficient keyword matching
- Daily stats auto-reset to keep data fresh

## 📱 How to Use

### View Stats
1. Swipe to the **Game Dashboard** screen (left swipe from home)
2. Scroll down to see **"YouTube Usage Tracker"** section
3. View:
   - Total watch time for today
   - Category breakdown pie chart
   - Top channels you watched
   - Recent video history

### Correct Categories
1. In the "Recent Videos" section, tap any video
2. A dialog will appear showing all categories
3. Select the correct category
4. The app learns and will auto-categorize similar videos correctly next time

### Reset Stats
- Stats automatically reset daily at midnight
- Keeps data fresh and relevant for daily tracking

## 🛠️ Technical Implementation

### Files Created/Modified

**New Files:**
- `data/model/YouTubeTracking.kt` - Data models
- `data/datastore/YouTubeTrackingDataStore.kt` - Storage layer

**Modified Files:**
- `service/SocialSentryAccessibilityService.kt` - Added tracking logic
- `presentation/ui/screen/GameDashboardScreen.kt` - Added UI components
- `di/AppModule.kt` - Registered dependencies

### Dependencies Used
- Koin (Dependency Injection) ✅
- Jetpack Compose (UI) ✅
- DataStore (Storage) ✅
- Kotlin Coroutines (Async operations) ✅
- AccessibilityService (Video detection) ✅

## 🎓 Use Cases

### For Students
- Track study vs entertainment balance
- See how much time spent on educational content
- Identify productive channels vs distracting ones
- Set study goals based on actual data

### For Self-Improvement
- Understand video consumption habits
- Reduce time on entertainment/gaming
- Increase productive/educational content
- Visual feedback through pie charts

### For Productivity
- Monitor daily YouTube usage
- Category-wise time tracking
- Identify top time-consuming channels
- Make data-driven decisions about content consumption

## 🚀 Future Enhancements (Optional)

1. **Weekly/Monthly Stats**: Aggregate stats over longer periods
2. **Goals & Alerts**: Set daily limits per category with notifications
3. **Export Data**: CSV export for external analysis
4. **Insights**: "You watched 2h more study content than last week!"
5. **Channel Tagging**: Mark specific channels as study/entertainment
6. **Watch History Search**: Search through watched videos

## 📝 Notes

- The tracker works only when **AccessibilityService is enabled**
- Requires YouTube app (not YouTube in browser)
- Title extraction may fail if YouTube updates their UI (multiple fallback methods implemented)
- Categories can be manually corrected anytime
- Data is stored locally and never leaves the device

---

**Implementation Status**: ✅ **COMPLETE**

All features implemented, tested, and integrated into the Game Dashboard screen. The system is ready to track YouTube usage and provide valuable insights!

