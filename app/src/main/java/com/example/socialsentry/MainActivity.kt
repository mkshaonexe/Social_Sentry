package com.example.socialsentry

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.example.socialsentry.domain.SocialSentryNotificationManager
import com.example.socialsentry.presentation.ui.screen.BlockScrollScreen
import com.example.socialsentry.presentation.ui.screen.GameDashboardScreen
import com.example.socialsentry.presentation.ui.screen.SettingsScreen
import com.example.socialsentry.ui.theme.SocialSentryTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    
    private lateinit var notificationManager: SocialSentryNotificationManager
    
    // Permission launcher for notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, notification system is ready
            android.util.Log.d("MainActivity", "Notification permission granted")
        } else {
            // Permission denied, show toast to explain
            android.widget.Toast.makeText(
                this, 
                "Notifications disabled. You won't get alerts when time is up.", 
                android.widget.Toast.LENGTH_LONG
            ).show()
            android.util.Log.d("MainActivity", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        notificationManager = SocialSentryNotificationManager(this)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Configure window to handle system bars properly
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            SocialSentryTheme {
                var showSettings by remember { mutableStateOf(false) }
                
                // Check and request notification permission when app opens
                LaunchedEffect(Unit) {
                    if (!notificationManager.hasNotificationPermission()) {
                        requestNotificationPermission()
                    }
                }
                
                if (showSettings) {
                    SettingsScreen(
                        onNavigateBack = { showSettings = false }
                    )
                } else {
                    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> GameDashboardScreen()
                            1 -> BlockScrollScreen(
                                onNavigateToSettings = { showSettings = true },
                                onRequestNotificationPermission = { requestNotificationPermission() }
                            )
                            2 -> HelloWorldPage(text = "Hello World 2")
                        }
                    }
                }
            }
        }
    }
    
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
private fun HelloWorldPage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}