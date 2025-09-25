package com.example.socialsentry.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.socialsentry.data.model.SocialApp
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: SocialSentryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var manualMinutes by remember { mutableStateOf("") }
    var reminderMinutes by remember { mutableStateOf(settings.usageReminderMinutes.toString()) }

    val socialApps = listOf(
        settings.youtube,
        settings.facebook,
        settings.instagram,
        settings.tiktok,
        settings.threads
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
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
                Text("Admin Panel", style = MaterialTheme.typography.headlineMedium)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Allowance Time", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = manualMinutes,
                            onValueChange = { manualMinutes = it },
                            label = { Text("Minutes to Add/Remove") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val minutes = manualMinutes.toIntOrNull() ?: 0
                            viewModel.addManualUnblockMinutes(minutes)
                        }) {
                            Text("Update Allowance")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Usage Reminder Time", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = reminderMinutes,
                            onValueChange = { reminderMinutes = it },
                            label = { Text("Minutes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val minutes = reminderMinutes.toIntOrNull() ?: 5
                            viewModel.updateUsageReminderTime(minutes)
                        }) {
                            Text("Set Reminder Time")
                        }
                    }
                }
            }

            items(socialApps) { app ->
                AppTimeControl(app = app, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppTimeControl(app: SocialApp, viewModel: SocialSentryViewModel) {
    var startTime by remember { mutableStateOf(app.blockTimeStart.toFloat()) }
    var endTime by remember { mutableStateOf(app.blockTimeEnd.toFloat()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(app.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Block Start Time: ${minutesToTime(startTime.toInt())}")
            Slider(
                value = startTime,
                onValueChange = { startTime = it },
                valueRange = 0f..1439f,
                onValueChangeFinished = {
                    viewModel.updateAppTimeRange(app.name, startTime.toInt(), endTime.toInt())
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Block End Time: ${minutesToTime(endTime.toInt())}")
            Slider(
                value = endTime,
                onValueChange = { endTime = it },
                valueRange = 0f..1439f,
                onValueChangeFinished = {
                    viewModel.updateAppTimeRange(app.name, startTime.toInt(), endTime.toInt())
                }
            )
        }
    }
}

private fun minutesToTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return String.format("%02d:%02d", hours, mins)
}
