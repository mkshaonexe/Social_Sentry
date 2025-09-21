package com.example.socialsentry.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.socialsentry.data.model.WorkoutSettings
import com.example.socialsentry.presentation.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSettingsScreen(
    workoutViewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by workoutViewModel.uiState.collectAsStateWithLifecycle()
    
    var dailyLimit by remember { mutableStateOf(uiState.dailyLimitMinutes) }
    var minutesPerPushUp by remember { mutableStateOf(uiState.minutesPerPushUp) }
    var isEnabled by remember { mutableStateOf(true) }
    
    LaunchedEffect(uiState) {
        dailyLimit = uiState.dailyLimitMinutes
        minutesPerPushUp = uiState.minutesPerPushUp
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) {
                Text("Back", color = Color(0xFF2196F3))
            }
            
            Text(
                text = "Workout Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Progress",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Push-ups",
                        value = uiState.totalPushUpsCompleted.toString(),
                        color = Color.White
                    )
                    
                    StatItem(
                        label = "Minutes Earned",
                        value = uiState.totalMinutesEarned.toString(),
                        color = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Settings Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Workout Configuration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Minutes per Push-up
                Text(
                    text = "Minutes earned per push-up:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(1, 2, 3, 5).forEach { value ->
                        FilterChip(
                            onClick = { minutesPerPushUp = value },
                            label = { Text("$value min") },
                            selected = minutesPerPushUp == value,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Daily Limit
                Text(
                    text = "Daily limit (0 = unlimited):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(0, 30, 60, 120).forEach { value ->
                        FilterChip(
                            onClick = { dailyLimit = value },
                            label = { Text(if (value == 0) "Unlimited" else "${value}min") },
                            selected = dailyLimit == value,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Save Button
        Button(
            onClick = {
                val newSettings = WorkoutSettings(
                    isEnabled = isEnabled,
                    dailyLimitMinutes = dailyLimit,
                    minutesPerPushUp = minutesPerPushUp,
                    totalMinutesEarned = uiState.totalMinutesEarned,
                    totalPushUpsCompleted = uiState.totalPushUpsCompleted,
                    lastWorkoutDate = "",
                    currentSessionMinutes = 0
                )
                workoutViewModel.updateWorkoutSettings(newSettings)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Save Settings", color = Color.White, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Display
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336))
            ) {
                Text(
                    text = error,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = color.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}
