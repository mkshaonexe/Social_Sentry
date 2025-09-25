package com.example.socialsentry.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import com.example.socialsentry.presentation.ui.components.AnimatedToggleSwitch
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import com.example.socialsentry.ui.theme.BrightPink
import com.example.socialsentry.ui.theme.DarkGray
import com.example.socialsentry.ui.theme.White
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.socialsentry.presentation.ui.pushup.PushUpCounterActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BlockScrollScreen(
    viewModel: SocialSentryViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    
    // Force recomposition when settings change
    LaunchedEffect(settings.remainingTemporaryUnblockMs) {
        // This will trigger recomposition when remaining time changes
        android.util.Log.d("BlockScrollScreen", "Settings updated - remainingMs: ${settings.remainingTemporaryUnblockMs}, isActive: ${settings.isTemporaryUnblockActive}")
    }
    val context = LocalContext.current
    var nowTick by remember { mutableStateOf(System.currentTimeMillis()) }
    var showAddTimeDialog by remember { mutableStateOf(false) }
    var addMinutesText by remember { mutableStateOf("") }
    var showDeveloperMode by remember { mutableStateOf(false) }
    var developerPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Push-up activity launcher
    val pushUpLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val count = result.data?.getIntExtra("push_up_count", 0) ?: 0
            if (count > 0) {
                scope.launch { 
                    viewModel.addMinutesFromPushUps(count)
                    Toast.makeText(context, "Added $count minutes from push-ups!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // Calculate if any reels/shorts are currently blocked by feature settings,
    // then derive effective blocking considering temporary unblock state
    val isBlockedByFeatures = settings.instagram.features.find { it.name == "Reels" }?.isEnabled == true ||
            settings.youtube.features.find { it.name == "Shorts" }?.isEnabled == true ||
            settings.facebook.features.find { it.name == "Reels" }?.isEnabled == true
    val isReelsBlocked = !settings.isTemporaryUnblockActive && isBlockedByFeatures
    
    var isToggleEnabled by remember { mutableStateOf(isReelsBlocked) }
    
    // Update toggle state when settings change
    LaunchedEffect(isReelsBlocked) {
        isToggleEnabled = isReelsBlocked
    }
    
    // Live ticker to update countdown while temporary unblock is active
    LaunchedEffect(settings.isTemporaryUnblockActive) {
        if (settings.isTemporaryUnblockActive) {
            while (true) {
                delay(1000)
                nowTick = System.currentTimeMillis()
            }
        }
    }

    val startEpoch = settings.temporaryUnblockSessionStartEpochMs
    val remainingMsActive = if (settings.isTemporaryUnblockActive && startEpoch != null) {
        val elapsed = (nowTick - startEpoch).coerceAtLeast(0L)
        val allowanceRemaining = (settings.remainingTemporaryUnblockMs - elapsed).coerceAtLeast(0L)
        val sessionCap = settings.maxTemporaryUnblockSessionMs.coerceAtLeast(0L)
        val sessionRemainingCap = (sessionCap - elapsed).coerceAtLeast(0L)
        minOf(allowanceRemaining, sessionRemainingCap)
    } else settings.remainingTemporaryUnblockMs
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGray)
            .systemBarsPadding()
    ) {
        // Settings button in top left
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = White,
                modifier = Modifier.size(28.dp)
            )
        }
        // Remaining time and Add button in top right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(remainingMsActive),
                color = White,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(
                onClick = { showAddTimeDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Time",
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            buildAnimatedText(
                text = "BLOCK SCROLL",
                isEnabled = isToggleEnabled
            )()
            
            // Subtitle
            AnimatedContent(
                targetState = isToggleEnabled,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { -it }
                    ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { it }
                    )
                },
                label = "subtitle"
            ) { enabled ->
                Text(
                    text = if (enabled) "Tap To Turn Off" else "Tap To Turn On",
                    fontSize = 18.sp,
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }
            
            // Animated Toggle Switch
            AnimatedToggleSwitch(
                isEnabled = isToggleEnabled,
                onToggle = {
                    android.util.Log.d("BlockScrollScreen", "Toggle pressed - isBlockedByFeatures: $isBlockedByFeatures, isTemporaryUnblockActive: ${settings.isTemporaryUnblockActive}")
                    
                    // Three cases:
                    // 1) Currently blocked by features (and not unblocked): start temporary unblock using allowance
                    // 2) Currently unblocked because of active temporary unblock: end the session (re-block)
                    // 3) Currently unblocked because features are disabled: enable features across apps
                    if (isBlockedByFeatures && !settings.isTemporaryUnblockActive) {
                        Toast.makeText(context, "Starting unblock session...", Toast.LENGTH_SHORT).show()
                        viewModel.startTemporaryUnblock(onInsufficientTime = {
                            Toast.makeText(
                                context,
                                "No remaining unblock time today — try again tomorrow.",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    } else if (settings.isTemporaryUnblockActive) {
                        Toast.makeText(context, "Ending unblock session...", Toast.LENGTH_SHORT).show()
                        viewModel.endTemporaryUnblock()
                    } else {
                        Toast.makeText(context, "Enabling reels blocking...", Toast.LENGTH_SHORT).show()
                        
                        // Auto-enable reels blocking for all platforms first
                        viewModel.toggleFeatureBlocking("Instagram", "Reels", true)
                        viewModel.toggleFeatureBlocking("YouTube", "Shorts", true)
                        viewModel.toggleFeatureBlocking("Facebook", "Reels", true)
                        
                        // Request notification permission and show notification
                        scope.launch {
                            val notificationManager = com.example.socialsentry.domain.SocialSentryNotificationManager(context)
                            
                            // Request permission first
                            onRequestNotificationPermission()
                            
                            // Wait a bit for permission dialog to be handled, then show notification
                            delay(1000)
                            
                            // Check if we have permission and show notification
                            if (notificationManager.hasNotificationPermission()) {
                                notificationManager.showReelsBlockedNotification()
                            } else {
                                // If no permission, show a toast instead
                                Toast.makeText(context, "✅ Reels blocking is now enabled!", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    // UI mirrors the settings by reacting to flow; do not flip immediately
                }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Status Card
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(500))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkGray.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        BrightPink.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(24.dp)
                            .heightIn(min = 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isToggleEnabled,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                                    animationSpec = tween(300),
                                    initialOffsetX = { -it }
                                ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                                    animationSpec = tween(300),
                                    targetOffsetX = { it }
                                )
                            },
                            label = "status"
                        ) { enabledState ->
                            buildAnimatedStatusText(enabledState)()
                        }
                    }
                }
            }
            
            // Countdown moved outside the status card, below it
            val timeText = formatDuration(remainingMsActive)
            val countdownText = if (!isReelsBlocked) {
                "Remaining today: $timeText"
            } else if (settings.isTemporaryUnblockActive) {
                "Unblock ends in: $timeText"
            } else null
            if (countdownText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = countdownText,
                    color = White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (showAddTimeDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddTimeDialog = false
                    showDeveloperMode = false
                    developerPassword = ""
                },
                confirmButton = {
                    if (showDeveloperMode) {
                        TextButton(onClick = {
                            if (developerPassword == "mkshaon31") {
                                val minutes = addMinutesText.toIntOrNull() ?: 0
                                if (minutes <= 0) {
                                    Toast.makeText(context, "Enter minutes > 0", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                                scope.launch {
                                    try {
                                        viewModel.addManualUnblockMinutes(minutes)
                                        Toast.makeText(context, "Added $minutes minutes successfully!", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error adding minutes: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                                showAddTimeDialog = false
                                showDeveloperMode = false
                                addMinutesText = ""
                                developerPassword = ""
                            } else {
                                Toast.makeText(context, "Incorrect password!", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Add")
                        }
                    } else {
                        // No confirm button when not in developer mode
                        Spacer(modifier = Modifier.width(0.dp))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showAddTimeDialog = false
                        showDeveloperMode = false
                        developerPassword = ""
                    }) { 
                        Text("Cancel") 
                    }
                },
                title = { Text("Add Unblock Time") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (!showDeveloperMode) {
                            // Manual entry section - locked
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manual Entry", style = MaterialTheme.typography.titleSmall)
                                IconButton(
                                    onClick = { showDeveloperMode = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Developer Mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            OutlinedTextField(
                                value = "",
                                onValueChange = { },
                                label = { Text("Minutes") },
                                singleLine = true,
                                enabled = false,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 5, 10).forEach { quick ->
                                    OutlinedButton(
                                        onClick = { },
                                        enabled = false
                                    ) {
                                        Text(text = "+${quick}m")
                                    }
                                }
                            }
                        } else {
                            // Developer mode - show password field and enabled controls
                            Text("Developer Mode", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = developerPassword,
                                onValueChange = { developerPassword = it },
                                label = { Text("Developer Password") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                            
                            OutlinedTextField(
                                value = addMinutesText,
                                onValueChange = { input ->
                                    addMinutesText = input.filter { it.isDigit() }.take(3)
                                },
                                label = { Text("Minutes") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 5, 10).forEach { quick ->
                                    OutlinedButton(onClick = {
                                        val current = addMinutesText.toIntOrNull() ?: 0
                                        addMinutesText = (current + quick).toString()
                                    }) {
                                        Text(text = "+${quick}m")
                                    }
                                }
                            }
                        }
                        
                        // Push-up option - always available
                        HorizontalDivider()
                        Text("Earn Time", style = MaterialTheme.typography.titleSmall)
                        Button(
                            onClick = {
                                showAddTimeDialog = false
                                showDeveloperMode = false
                                developerPassword = ""
                                val intent = Intent(context, PushUpCounterActivity::class.java)
                                pushUpLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🏋️ Do Push-ups (1 push-up = 1 minute)")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun buildAnimatedText(text: String, isEnabled: Boolean): @Composable () -> Unit {
    val words = text.split(" ")
    return {
        AnimatedText(
            words = words.mapIndexed { index, word ->
                AnimatedWord(
                    text = word,
                    color = if (index == 0) BrightPink else White,
                    isEnabled = isEnabled
                )
            }
        )
    }
}

@Composable
private fun buildAnimatedStatusText(isEnabled: Boolean): @Composable () -> Unit {
    val text = if (isEnabled) "Scrolling is Blocked" else "Scrolling is Unblocked"
    val words = text.split(" ")
    return {
        AnimatedText(
            words = words.mapIndexed { index, word ->
                AnimatedWord(
                    text = word,
                    color = if (word == "Blocked" || word == "Unblocked") BrightPink else White,
                    isEnabled = isEnabled
                )
            }
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedText(words: List<AnimatedWord>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        words.forEach { word ->
            AnimatedContent(
                targetState = word,
                transitionSpec = {
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) togetherWith scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                },
                label = "word"
            ) { animatedWord ->
                Text(
                    text = animatedWord.text,
                    color = animatedWord.color,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class AnimatedWord(
    val text: String,
    val color: Color,
    val isEnabled: Boolean
)