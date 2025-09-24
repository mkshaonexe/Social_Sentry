package com.example.socialsentry.presentation.ui.pushup

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.socialsentry.ui.theme.SocialSentryTheme
import java.util.concurrent.Executors

class PushUpCounterActivity : ComponentActivity() {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SocialSentryTheme {
                var count by remember { mutableStateOf(0) }
                val detector = remember { ImprovedPushUpDetector() }
                var hasCameraPermission by remember { mutableStateOf(false) }
                var isUsingFrontCamera by remember { mutableStateOf(true) }
                var currentPose by remember { mutableStateOf<com.google.mlkit.vision.pose.Pose?>(null) }

                val activity = LocalContext.current as Activity
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    hasCameraPermission = granted
                    if (!granted) activity.finish()
                }

                LaunchedEffect(Unit) {
                    hasCameraPermission = ContextCompat.checkSelfPermission(
                        this@PushUpCounterActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasCameraPermission) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (hasCameraPermission) {
                            AndroidPreview(
                                modifier = Modifier.fillMaxSize(),
                                isUsingFrontCamera = isUsingFrontCamera,
                onPose = { pose, imageWidth, imageHeight ->
                    currentPose = pose
                    count = detector.onPose(pose)
                }
                            )
                        }

                        // Camera switch button
                        Button(
                            onClick = { isUsingFrontCamera = !isUsingFrontCamera },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = if (isUsingFrontCamera) "Back" else "Front",
                                color = Color.White
                            )
                        }

                        // Push-up counter and form feedback
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Form feedback
                            val formFeedback = getFormFeedback(currentPose)
                            if (formFeedback.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x80000000))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = formFeedback,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // Counter
                            Box(
                                modifier = Modifier
                                    .background(Color(0x80000000))
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text(text = "Push-ups: $count", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                            }
                        }

                        ExtendedFloatingActionButton(
                            onClick = {
                                setResult(RESULT_OK, android.content.Intent().putExtra("push_up_count", count))
                                finish()
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    // Camera is fully managed inside AndroidPreview composable
}

private fun getFormFeedback(pose: com.google.mlkit.vision.pose.Pose?): String {
    if (pose == null) return "Position yourself in camera view"
    
    val leftShoulder = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.LEFT_SHOULDER)
    val rightShoulder = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.RIGHT_SHOULDER)
    val leftElbow = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.LEFT_ELBOW)
    val rightElbow = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.RIGHT_ELBOW)
    val leftWrist = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.LEFT_WRIST)
    val rightWrist = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.RIGHT_WRIST)
    
    if (leftShoulder == null || rightShoulder == null || leftElbow == null || rightElbow == null || leftWrist == null || rightWrist == null) {
        return "Keep your upper body visible"
    }
    
    // Calculate elbow angles
    val leftAngle = calculateElbowAngle(leftShoulder, leftElbow, leftWrist)
    val rightAngle = calculateElbowAngle(rightShoulder, rightElbow, rightWrist)
    
    val avgAngle = (leftAngle + rightAngle) / 2.0
    
    return when {
        avgAngle < 70 -> "Perfect down position! 💪"
        avgAngle > 130 -> "Good up position! 👍"
        avgAngle in 70.0..130.0 -> "Keep your form steady"
        else -> "Get into push-up position"
    }
}

private fun calculateElbowAngle(
    shoulder: com.google.mlkit.vision.pose.PoseLandmark,
    elbow: com.google.mlkit.vision.pose.PoseLandmark,
    wrist: com.google.mlkit.vision.pose.PoseLandmark
): Double {
    val v1x = shoulder.position.x - elbow.position.x
    val v1y = shoulder.position.y - elbow.position.y
    val v2x = wrist.position.x - elbow.position.x
    val v2y = wrist.position.y - elbow.position.y
    
    val dot = v1x * v2x + v1y * v2y
    val mag1 = kotlin.math.sqrt(v1x * v1x + v1y * v1y)
    val mag2 = kotlin.math.sqrt(v2x * v2x + v2y * v2y)
    
    if (mag1 == 0f || mag2 == 0f) return 0.0
    
    val cos = (dot / (mag1 * mag2)).coerceIn(-1f, 1f)
    return Math.toDegrees(kotlin.math.acos(cos.toDouble()))
}

@Composable
private fun AndroidPreview(
    modifier: Modifier = Modifier,
    isUsingFrontCamera: Boolean = false,
    onPose: (com.google.mlkit.vision.pose.Pose, Int, Int) -> Unit
) {
    val context = LocalContext.current
    var overlayView by remember { mutableStateOf<PoseOverlayView?>(null) }
    
    AndroidView(
        factory = {
            val frameLayout = android.widget.FrameLayout(context)
            val previewView = PreviewView(context).apply {
                // Fill the entire view with the camera stream to avoid letterboxing
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            val overlay = PoseOverlayView(context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            
            frameLayout.addView(previewView)
            frameLayout.addView(overlay)
            overlayView = overlay
            
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                val cameraProvider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                val analyzer = CameraPoseAnalyzer { pose, imageWidth, imageHeight -> 
                    onPose(pose, imageWidth, imageHeight)
                    // Update skeleton overlay with actual image dimensions
                    overlay.post {
                        overlay.updatePose(pose, imageWidth, imageHeight, isUsingFrontCamera)
                    }
                }
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
                
                val cameraSelector = if (isUsingFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as androidx.lifecycle.LifecycleOwner,
                        cameraSelector,
                        preview,
                        analysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
            frameLayout
        },
        update = { frameLayout ->
            // Update camera when front/back camera changes
            val context = frameLayout.context
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                val cameraProvider = providerFuture.get()
                val previewView = frameLayout.getChildAt(0) as PreviewView
                val overlay = frameLayout.getChildAt(1) as PoseOverlayView
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                val analyzer = CameraPoseAnalyzer { pose, imageWidth, imageHeight -> 
                    onPose(pose, imageWidth, imageHeight)
                    overlay.post {
                        overlay.updatePose(pose, imageWidth, imageHeight, isUsingFrontCamera)
                    }
                }
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
                
                val cameraSelector = if (isUsingFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as androidx.lifecycle.LifecycleOwner,
                        cameraSelector,
                        preview,
                        analysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        },
        modifier = modifier
    )
}


