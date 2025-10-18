# App Version Display Feature

## Overview
Added app version and last update date display to the settings screen, accessible through the 3-line menu button.

## Implementation Details

### What Was Added
1. **Version Information Card** - A new card in the settings screen that displays:
   - App version: **0.3.1 beta**
   - Last update date: **December 2024**

2. **Visual Design**:
   - Clean, centered layout with proper spacing
   - Version number highlighted in cyan color (`#00BCD4`)
   - Consistent with existing settings card design
   - Uses Material Design 3 typography

### Code Changes

**File Modified**: `app/src/main/java/com/example/socialsentry/presentation/ui/screen/SettingsScreen.kt`

**Added Components**:
1. **Version Information Card** (lines 624-661):
   ```kotlin
   // App Version Information
   item {
       Card(
           modifier = Modifier
               .fillMaxWidth()
               .clip(RoundedCornerShape(16.dp)),
           colors = CardDefaults.cardColors(
               containerColor = MaterialTheme.colorScheme.surface
           )
       ) {
           Column(
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(20.dp),
               horizontalAlignment = Alignment.CenterHorizontally
           ) {
               Text(
                   text = "App Version",
                   style = MaterialTheme.typography.titleMedium,
                   fontWeight = FontWeight.Bold,
                   color = MaterialTheme.colorScheme.onBackground
               )
               Spacer(modifier = Modifier.height(8.dp))
               Text(
                   text = "0.3.1 beta",
                   style = MaterialTheme.typography.titleLarge,
                   fontWeight = FontWeight.Bold,
                   color = Color(0xFF00BCD4)
               )
               Spacer(modifier = Modifier.height(4.dp))
               Text(
                   text = "Last updated: ${getLastUpdateDate()}",
                   style = MaterialTheme.typography.bodySmall,
                   color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
               )
           }
       }
   }
   ```

2. **Helper Function** (lines 953-956):
   ```kotlin
   private fun getLastUpdateDate(): String {
       // Return the last update date - you can update this when you release new versions
       return "December 2024"
   }
   ```

### Location in Settings
The version information card appears in the settings screen:
1. **Reels Block** (expandable)
2. **Scroll Limiter** (expandable) 
3. **Developer Mode** (expandable)
4. **App Version** ← **NEW** (static display)
5. Developer Mode expanded content (when enabled)

### How to Update Version
To update the version information in future releases:

1. **Update Version Number**: Change the hardcoded "0.3.1 beta" text in the version card
2. **Update Last Update Date**: Modify the `getLastUpdateDate()` function to return the new date

### Example Update:
```kotlin
// In the version card
Text(
    text = "0.4.0", // Updated version
    // ... rest of the code
)

// In getLastUpdateDate() function
private fun getLastUpdateDate(): String {
    return "January 2025" // Updated date
}
```

### Visual Appearance
- **Card Style**: Rounded corners (16dp), matches other settings cards
- **Layout**: Centered column with proper spacing
- **Typography**: 
  - "App Version" - Medium title, bold
  - "0.3.1 beta" - Large title, bold, cyan color
  - "Last updated: December 2024" - Small body text, 70% opacity
- **Spacing**: 8dp between title and version, 4dp between version and date

### User Experience
- Users can now easily see the current app version
- Version information is prominently displayed in settings
- Consistent with the app's design language
- No interaction required - purely informational display

## Testing
The version information should be visible when:
1. Opening the app
2. Clicking the 3-line menu button (top-left)
3. Navigating to Settings
4. Scrolling down to see the "App Version" card

The card should display:
- "App Version" as the title
- "0.3.1 beta" in cyan color
- "Last updated: December 2024" in smaller, dimmed text
