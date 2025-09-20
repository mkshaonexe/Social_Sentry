package com.example.socialsentry.presentation.ui.screen

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
<<<<<<< HEAD
import androidx.compose.foundation.layout.*
=======
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
>>>>>>> master
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
<<<<<<< HEAD
=======
import androidx.compose.ui.Alignment
>>>>>>> master
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
<<<<<<< HEAD
=======
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
>>>>>>> master
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.socialsentry.data.model.SocialApp
import com.example.socialsentry.presentation.ui.components.AccessibilityServiceCard
import com.example.socialsentry.presentation.ui.components.SocialAppCard
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
<<<<<<< HEAD
    viewModel: SocialSentryViewModel = koinViewModel()
=======
    viewModel: SocialSentryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
>>>>>>> master
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    
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
<<<<<<< HEAD
        settings.instagram,
        settings.youtube,
        settings.tiktok,
        settings.facebook
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Social Sentry",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Text(
                text = "Block distracting content on social media apps",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
=======
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
                imageVector = Icons.Rounded.ArrowBack,
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
>>>>>>> master
        item {
            AccessibilityServiceCard(
                isServiceEnabled = isAccessibilityEnabled
            )
        }
        
        item {
            Text(
                text = "Managed Apps",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
<<<<<<< HEAD
=======
                color = MaterialTheme.colorScheme.onBackground,
>>>>>>> master
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
         items(socialApps) { app ->
             SocialAppCard(
                 app = app,
                 isAccessibilityEnabled = isAccessibilityEnabled,
                 onToggleBlocking = { isBlocked ->
                     viewModel.toggleAppBlocking(app.name, isBlocked)
                 }
             )
         }
        
<<<<<<< HEAD
=======
        }
>>>>>>> master
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
