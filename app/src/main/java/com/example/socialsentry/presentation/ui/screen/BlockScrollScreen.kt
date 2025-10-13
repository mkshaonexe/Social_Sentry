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
import androidx.compose.material.icons.rounded.Warning
import com.example.socialsentry.presentation.ui.components.AnimatedToggleSwitch
import com.example.socialsentry.presentation.ui.components.PermissionWarningBanner
import com.example.socialsentry.presentation.ui.components.CompactPermissionWarning
import com.example.socialsentry.presentation.ui.components.PermissionDetailsDialog
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import com.example.socialsentry.util.PermissionChecker
import com.example.socialsentry.ui.theme.BrightPink
import com.example.socialsentry.ui.theme.BrightGreen
import com.example.socialsentry.ui.theme.DarkGray
import com.example.socialsentry.ui.theme.White
import com.example.socialsentry.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import java.util.concurrent.TimeUnit
import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var showAccessibilityWarningDialog by remember { mutableStateOf(false) }
    var showMoreInfo by remember { mutableStateOf(false) }
    var nowTick by remember { mutableStateOf(System.currentTimeMillis()) }
    var showAddTimeDialog by remember { mutableStateOf(false) }
    var addMinutesText by remember { mutableStateOf("") }
    var showDeveloperMode by remember { mutableStateOf(false) }
    var developerPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Check permission status
    var permissionStatus by remember { mutableStateOf(PermissionChecker.getPermissionStatus(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Check accessibility service status
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                isAccessibilityEnabled = context.isAccessibilityServiceEnabled()
                permissionStatus = PermissionChecker.getPermissionStatus(context)
            }
            else -> {}
        }
    }
    
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
        
        // Warning icon when permissions are missing
        if (!permissionStatus.hasAllPermissions) {
            CompactPermissionWarning(
                permissionStatus = permissionStatus,
                onClick = { showPermissionDialog = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
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
                text = "Social Sentry",
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
                        Button(
                            onClick = {
                                if (developerPassword == "mkshaon31") {
                                    val minutes = addMinutesText.toIntOrNull() ?: 0
                                    if (minutes <= 0) {
                                        Toast.makeText(context, "Enter minutes > 0", Toast.LENGTH_SHORT).show()
                                        return@Button
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
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00BCD4)
                            )
                        ) {
                            Text("Add Time")
                        }
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
                title = { 
                    Text(
                        "Add Unblock Time",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Manual Entry Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (showDeveloperMode) 
                                    Color(0xFF00BCD4).copy(alpha = 0.1f) 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (showDeveloperMode) Icons.Rounded.Lock else Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = if (showDeveloperMode) Color(0xFF00BCD4) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (showDeveloperMode) "Developer Mode" else "Manual Entry",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (!showDeveloperMode) {
                                        IconButton(
                                            onClick = { showDeveloperMode = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Lock,
                                                contentDescription = "Unlock",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                
                                if (showDeveloperMode) {
                                    // Developer mode - show password field
                                    OutlinedTextField(
                                        value = developerPassword,
                                        onValueChange = { developerPassword = it },
                                        label = { Text("Password") },
                                        placeholder = { Text("Enter developer password") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BCD4),
                                            focusedLabelColor = Color(0xFF00BCD4)
                                        )
                                    )
                                    
                                    OutlinedTextField(
                                        value = addMinutesText,
                                        onValueChange = { input ->
                                            addMinutesText = input.filter { it.isDigit() }.take(3)
                                        },
                                        label = { Text("Minutes") },
                                        placeholder = { Text("0") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BCD4),
                                            focusedLabelColor = Color(0xFF00BCD4)
                                        )
                                    )
                                    
                                    // Quick add buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf(1, 5, 10).forEach { quick ->
                                            OutlinedButton(
                                                onClick = {
                                                    val current = addMinutesText.toIntOrNull() ?: 0
                                                    addMinutesText = (current + quick).toString()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color(0xFF00BCD4)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00BCD4))
                                            ) {
                                                Text("+${quick}m")
                                            }
                                        }
                                    }
                                } else {
                                    // Locked state
                                    Text(
                                        text = "🔒 Locked for manual time entry",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        // Earn Time Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF00BCD4).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF00BCD4),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Earn Time",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        showAddTimeDialog = false
                                        showDeveloperMode = false
                                        developerPassword = ""
                                        val intent = Intent(context, PushUpCounterActivity::class.java)
                                        pushUpLauncher.launch(intent)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00BCD4)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 8.dp
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🏋️",
                                            fontSize = 24.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = "Do Push-ups",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "1 push-up = 1 minute unblock time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF00BCD4),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
        
        // Accessibility Warning Dialog
        if (showAccessibilityWarningDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAccessibilityWarningDialog = false
                    showMoreInfo = false
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showAccessibilityWarningDialog = false
                        showMoreInfo = false
                        onNavigateToSettings()
                    }) {
                        Text("Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showAccessibilityWarningDialog = false
                        showMoreInfo = false
                    }) {
                        Text("Dismiss")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { 
                    Text(
                        text = "Setup Not Complete",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "The app is not set up correctly. Accessibility service permission is required for the app to function properly.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Without this permission, the app cannot:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Block social media scrolling",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            text = "• Monitor app usage",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            text = "• Enforce time restrictions",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please enable the accessibility service in Settings to use this app.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = BrightPink
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // See more section
                        TextButton(
                            onClick = { showMoreInfo = !showMoreInfo },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showMoreInfo) "See less ▲" else "See more ▼",
                                color = Color(0xFF00BCD4),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        AnimatedVisibility(visible = showMoreInfo) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E1E))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📺 Tutorial & Support",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00BCD4)
                                )
                                
                                HorizontalDivider(
                                    color = Color(0xFF333333),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                
                                // YouTube
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "▶️ YouTube:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = White,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    Text(
                                        text = "@mkshaon7",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                                
                                // Email
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "📧 Email:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = White,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    Text(
                                        text = "mkshaon2024@gmail.com",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                                
                                // Website
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🌐 Website:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = White,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    Text(
                                        text = "mkshaon.com/reels_blocker",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2196F3)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "For detailed setup tutorial, check out the resources above!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            )
        }
        
        // Permission Details Dialog
        if (showPermissionDialog) {
            PermissionDetailsDialog(
                permissionStatus = permissionStatus,
                onDismiss = { showPermissionDialog = false }
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
                    color = when {
                        // When enabled (ON): "Social" = White, "Sentry" = Green
                        isEnabled && index == 0 -> White
                        isEnabled && index == 1 -> BrightGreen
                        // When disabled (OFF): "Social" = Pink/Red, "Sentry" = White
                        !isEnabled && index == 0 -> BrightPink
                        else -> White
                    },
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

private fun Context.isAccessibilityServiceEnabled(): Boolean {
    val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
        AccessibilityEvent.TYPE_VIEW_CLICKED
    )
    
    val targetServiceId = "com.example.socialsentry/.service.SocialSentryAccessibilityService"
    val isEnabled = enabledServices.any { serviceInfo ->
        serviceInfo.id == targetServiceId
    }
    
    android.util.Log.d("BlockScrollScreen", "Accessibility service enabled: $isEnabled")
    return isEnabled
}