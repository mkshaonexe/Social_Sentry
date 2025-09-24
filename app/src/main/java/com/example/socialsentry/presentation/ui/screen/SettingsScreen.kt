package com.example.socialsentry.presentation.ui.screen

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
            Text(
                text = "Managed Apps",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
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
        
        // Temporary Unblock section hidden as requested
        /*
        item {
            Text(
                text = "Temporary Unblock",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
            val scope = rememberCoroutineScope()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily allowance (minutes)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var minutesText by remember(settings.dailyTemporaryUnblockMinutes) { mutableStateOf(settings.dailyTemporaryUnblockMinutes.toString()) }
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { new ->
                            val filtered = new.filter { it.isDigit() }.take(3)
                            minutesText = filtered
                            val value = filtered.toIntOrNull()
                            if (value != null && value in 0..180) {
                                val current = viewModel.settings.value
                                val updated = current.copy(
                                    dailyTemporaryUnblockMinutes = value,
                                    remainingTemporaryUnblockMs = current.remainingTemporaryUnblockMs.coerceAtMost(value.toLong() * 60_000L)
                                )
                                scope.launch { viewModel.updateSettingsDirect(updated) }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Resets at local midnight.",
                        style = MaterialTheme.typography.bodyMedium
                    )
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = "Each unblock session automatically re-enables blocking after 10 minutes.",
					style = MaterialTheme.typography.bodySmall
				)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Add time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var manualText by remember { mutableStateOf("") }
                    val activity = LocalContext.current as Activity
                    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            val count = result.data?.getIntExtra("push_up_count", 0) ?: 0
                            if (count > 0) {
                                scope.launch { viewModel.addMinutesFromPushUps(count) }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualText,
                            onValueChange = { new ->
                                manualText = new.filter { it.isDigit() }.take(3)
                            },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            val m = manualText.toIntOrNull()?.coerceIn(0, 180) ?: 0
                            if (m > 0) {
                                scope.launch { viewModel.addManualUnblockMinutes(m) }
                                manualText = ""
                            }
                        }) {
                            Text("Add manually")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val intent = Intent(activity, PushUpCounterActivity::class.java)
                        launcher.launch(intent)
                    }) {
                        Text("Add via push-ups")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val snackbarHostState = remember { SnackbarHostState() }
                    val localScope = rememberCoroutineScope()
                    LaunchedEffect(settings.remainingTemporaryUnblockMs) {
                        // No-op placeholder; could be used to react to updates
                    }
                    // Show a snackbar after manual add
                    DisposableEffect(Unit) {
                        onDispose {}
                    }
                }
            }
        }
        */
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
