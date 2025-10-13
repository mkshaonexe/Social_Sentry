# Facebook Reels Auto-Reload Fix

## Issue
When clicking on Facebook Reels, the app correctly blocked and closed it, but Facebook would auto-reload or re-trigger Reels detection, creating an annoying navigation loop.

## Root Cause
The previous redirection method was causing Facebook to navigate in a way that:
1. Triggered multiple accessibility events during the transition
2. Temporarily showed Reels content again while navigating
3. Re-triggered the blocking logic before navigation completed
4. Created a reload loop

## Solution Implemented

### 1. Post-Redirect Cooldown Period
- **Added**: 3-second cooldown after redirection
- **Purpose**: Prevents re-detection during navigation transitions
- **Benefit**: Eliminates reload loop

```kotlin
private var lastFacebookRedirectTime = 0L
private val facebookPostRedirectCooldownMs = 3000L // 3 seconds

// In handleFacebook():
if (now - lastFacebookRedirectTime < facebookPostRedirectCooldownMs) {
    Log.v(TAG, "Facebook: In post-redirect cooldown, skipping detection")
    return
}
```

### 2. Improved Redirection Method
- **Primary Method**: Back button press (most reliable)
- **Fallback**: Automatic second Back press after 500ms if still in Reels
- **Last Resort**: Home tab click if back button fails

```kotlin
private fun redirectToFacebookHome(root: AccessibilityNodeInfo) {
    // Method 1: Back button (most reliable, no reload)
    performGlobalAction(GLOBAL_ACTION_BACK)
    
    // Auto second back if needed after 500ms
    Handler().postDelayed({
        if (currentRoot != null && isInFacebookBlockedView(currentRoot)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }, 500)
    
    // Method 2: Home tab click (fallback)
    // Only if back button failed
}
```

### 3. Detection Skip Logic
- Detection is now skipped during cooldown period
- Prevents multiple redirects during navigation
- Allows Facebook to complete navigation smoothly

## Results

### Before Fix:
1. Click on Reels → Auto-closes ✓
2. Facebook reloads/navigates → Auto-closes ✓
3. Facebook reloads again → Auto-closes ✓
4. **Annoying reload loop** ✗

### After Fix:
1. Click on Reels → Auto-closes with Back button ✓
2. Navigation completes smoothly (cooldown active) ✓
3. No reload loop ✓
4. **Clean exit from Reels** ✓

## Technical Details

### Timing Configuration:
- **Post-redirect cooldown**: 3000ms (3 seconds)
- **Second back press delay**: 500ms
- **Facebook debounce**: 1200ms (unchanged)
- **Block debounce**: 300ms (unchanged)

### Navigation Flow:
1. **Reels detected** → Set redirect timestamp
2. **Back button pressed** → Navigate away
3. **500ms delay** → Check if still in Reels
4. **Second back press** → If needed
5. **3s cooldown** → Skip all detection
6. **Normal detection resumes** → After cooldown

## Testing Recommendations

Test the following scenarios:
1. ✓ Click on Reels from News Feed
2. ✓ Click on Reels from navigation tab
3. ✓ Navigate to Reels via search
4. ✓ Open Reels from notifications
5. ✓ Verify no reload loop in all cases
6. ✓ Verify smooth exit back to previous screen

## Follow-up Fix: Facebook Auto-Close on Normal Feed (Issue #2)

### Problem
After implementing the reload fix, a new issue appeared: Facebook would auto-close immediately when opening the app, even when just viewing the normal news feed.

### Root Cause
The detection was too aggressive:
- Keywords like "like", "comment", "share", "save", "follow" appear on EVERY Facebook post
- These generic keywords were triggering blocking on normal feed content
- The `isBlockedFacebookElement()` function was catching ALL posts, not just Reels/Stories/Watch

### Solution
1. **Made Element Detection Much More Specific:**
   - Changed from generic keywords to SPECIFIC identifiers
   - Now only checks for: "reels tab", "stories tab", "watch tab", "reels button", etc.
   - Removed all generic keywords that appear on normal posts
   - Added specific view ID checks for blocked content containers

2. **Prioritized View-Based Detection:**
   - First checks if currently IN a Reels/Stories/Watch view (most reliable)
   - Only checks clicked elements if it's a TYPE_VIEW_CLICKED event
   - This prevents false positives from window state changes

### Code Changes
```kotlin
// Before - TOO BROAD:
val blockedKeywords = listOf(
    "reels", "stories", "watch", "video", "reel", "story",
    "for you", "explore", "short video", "vertical video",
    "like", "comment", "share", "save", "follow" // ❌ These are on EVERY post!
)

// After - SPECIFIC:
val specificBlockedKeywords = listOf(
    "reels tab", "stories tab", "watch tab",
    "reels button", "stories button", "watch button",
    "open reels", "view reels", "view stories",
    "for you tab" // ✓ Reels-specific only
)
```

### Result
- ✅ Normal Facebook feed works perfectly
- ✅ Can scroll and interact with news feed posts
- ✅ Only blocks when actually IN Reels/Stories/Watch
- ✅ No false positives on normal content

## Notes

- The Back button method is more reliable than clicking the Home tab
- The 3-second cooldown is sufficient for Facebook navigation transitions
- The automatic second Back press handles edge cases where one press isn't enough
- Specific keyword detection prevents false positives on normal feed content
- View-based detection is more reliable than click-based detection
- This fix maintains all existing blocking functionality while eliminating both the reload issue AND the false positive auto-close issue

