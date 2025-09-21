package com.example.socialsentry.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.socialsentry.presentation.ui.components.CameraPreview
import com.example.socialsentry.presentation.viewmodel.WorkoutViewModel
import com.example.socialsentry.service.PushUpDetector
import com.google.mlkit.vision.common.InputImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.socialsentry.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by workoutViewModel.uiState.collectAsStateWithLifecycle()
    
    var pushUpDetector by remember { mutableStateOf<PushUpDetector?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    LaunchedEffect(Unit) {
        pushUpDetector = PushUpDetector(context)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            pushUpDetector?.close()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Workout Session",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Camera Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            CameraPreview(
                onCameraReady = { view ->
                    previewView = view
                    setupImageAnalysis(view, pushUpDetector, workoutViewModel)
                }
            )
            
            // Overlay instructions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Position yourself in front of the camera\nfor push-up detection",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Push-ups Counter
            StatCard(
                title = "Push-ups",
                value = uiState.pushUpCount.toString(),
                subtitle = "Completed",
                color = Color(0xFF4CAF50)
            )
            
            // Minutes Earned
            StatCard(
                title = "Minutes",
                value = uiState.minutesEarned.toString(),
                subtitle = "Earned",
                color = Color(0xFF2196F3)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Session Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isWorkoutActive) 
                    Color(0xFF4CAF50) else Color(0xFF757575)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isWorkoutActive) "Session Active" else "Session Paused",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "1 push-up = ${uiState.minutesPerPushUp} minute(s)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (uiState.isWorkoutActive) {
                Button(
                    onClick = { workoutViewModel.pauseWorkout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pause", color = Color.White)
                }
            } else {
                Button(
                    onClick = { workoutViewModel.resumeWorkout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resume", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = { workoutViewModel.endWorkout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("End Session", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Start Button (if no active session)
        if (!uiState.isWorkoutActive && uiState.pushUpCount == 0) {
            Button(
                onClick = { workoutViewModel.startWorkout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Workout", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = Modifier.size(120.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}

private fun setupImageAnalysis(
    previewView: PreviewView,
    pushUpDetector: PushUpDetector?,
    workoutViewModel: WorkoutViewModel
) {
    // This would be implemented with CameraX ImageAnalysis
    // For now, we'll simulate the detection
    // In a real implementation, you'd set up the camera analysis pipeline here
}
