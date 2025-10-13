package com.example.socialsentry.presentation.ui.screen

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.socialsentry.data.model.SocialApp
import com.example.socialsentry.presentation.ui.components.AccessibilityServiceCard
import com.example.socialsentry.presentation.ui.components.SocialAppCard
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import com.example.socialsentry.presentation.ui.pushup.PushUpCounterActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SocialSentryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var isManagedAppsExpanded by remember { mutableStateOf(false) }
    
    // Check accessibility service status
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                isAccessibilityEnabled = context.isAccessibilityServiceEnabled()
            }
            else -> {}
        }
    }
    
    val socialApps = listOf(
        settings.youtube,
        settings.facebook,
        settings.instagram,
        settings.tiktok
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Back button in top left with proper spacing
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            AccessibilityServiceCard(
                isServiceEnabled = isAccessibilityEnabled
            )
        }
        
        item {
            // Managed Apps Header - Clickable to expand/collapse
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isManagedAppsExpanded = !isManagedAppsExpanded },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Managed Apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Icon(
                        imageVector = if (isManagedAppsExpanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isManagedAppsExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        
        // Expandable Apps List
        if (isManagedAppsExpanded) {
            items(socialApps) { app ->
                SocialAppCard(
                    app = app,
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    onToggleBlocking = { isBlocked ->
                        viewModel.toggleAppBlocking(app.name, isBlocked)
                    }
                )
            }
        }
        
        // Temporary Unblock section is hidden as requested
        
        // Scroll Limiter Feature Toggle
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Facebook Scroll Limiter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Shows a 30-second break popup after 1 minute of scrolling on Facebook",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Switch(
                            checked = settings.scrollLimiterEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateScrollLimiterEnabled(enabled)
                            },
                            enabled = isAccessibilityEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF4CAF50), // Green for enabled
                                checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color(0xFF9E9E9E), // Gray for disabled
                                uncheckedTrackColor = Color(0xFF9E9E9E).copy(alpha = 0.3f),
                                disabledCheckedThumbColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                                disabledCheckedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
                                disabledUncheckedThumbColor = Color(0xFF757575),
                                disabledUncheckedTrackColor = Color(0xFF757575).copy(alpha = 0.3f)
                            )
                        )
                    }
                    
                    if (!isAccessibilityEnabled) {
                        Text(
                            text = "⚠️ Accessibility service must be enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        }
    }
}

private fun Context.isAccessibilityServiceEnabled(): Boolean {
    val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
        AccessibilityEvent.TYPE_VIEW_CLICKED
    )
    
    // Debug: Log all enabled services
    enabledServices.forEach { service ->
        android.util.Log.d("SocialSentry", "Enabled accessibility service: ${service.id}")
    }
    
    val targetServiceId = "com.example.socialsentry/.service.SocialSentryAccessibilityService"
    val isEnabled = enabledServices.any { serviceInfo ->
        serviceInfo.id == targetServiceId
    }
    
    android.util.Log.d("SocialSentry", "Looking for service: $targetServiceId, found: $isEnabled")
    return isEnabled
}
