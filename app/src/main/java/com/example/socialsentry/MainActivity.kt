package com.example.socialsentry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
<<<<<<< HEAD
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
=======
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.example.socialsentry.presentation.ui.screen.BlockScrollScreen
>>>>>>> master
import com.example.socialsentry.presentation.ui.screen.SettingsScreen
import com.example.socialsentry.ui.theme.SocialSentryTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< HEAD
        enableEdgeToEdge()
        setContent {
            SocialSentryTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Social Sentry",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_shield),
                                    contentDescription = "App icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                ) { innerPadding ->
                    SettingsScreen()
=======
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Configure window to handle system bars properly
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            SocialSentryTheme {
                var showSettings by remember { mutableStateOf(false) }
                
                if (showSettings) {
                    SettingsScreen(
                        onNavigateBack = { showSettings = false }
                    )
                } else {
                    BlockScrollScreen(
                        onNavigateToSettings = { showSettings = true }
                    )
>>>>>>> master
                }
            }
        }
    }
}