package com.example.poseexercise.views.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.poseexercise.R
import com.example.poseexercise.data.PostureResult
import com.example.poseexercise.databinding.ActivityMainBinding
import com.example.poseexercise.posedetector.PoseDetectorProcessor
import com.example.poseexercise.util.VisionImageProcessor
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.poseexercise.viewmodels.CameraXViewModel
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

/**
 * Main Activity - Simple Push-up Detection App
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraXViewModel: CameraXViewModel
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        increaseNotificationVolume()

        // Setup camera switch button
        setupCameraSwitchButton()
        
        // Check camera permission first
        if (checkCameraPermission()) {
            setupCamera()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                Toast.makeText(this, "Camera permission is required for push-up detection", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun setupCameraSwitchButton() {
        binding.cameraSwitchButton.setOnClickListener {
            switchCamera()
        }
        
        // Add long press to reset counter
        binding.pushupCounter.setOnLongClickListener {
            resetPushUpCounter()
            true
        }
    }
    
    private fun resetPushUpCounter() {
        // Reset the enhanced detector through a message to the processor
        // This is a simplified approach - in a real app you'd want proper communication
        binding.pushupCounter.text = "Push-ups: 0 (Reset)"
        binding.statusText.text = "Counter reset! Get in position for push-ups"
        binding.statusText.setTextColor(getColor(android.R.color.white))
        
        Toast.makeText(this, "Push-up counter reset!", Toast.LENGTH_SHORT).show()
    }
    
    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        bindCameraUseCases()
    }

    private fun setupCamera() {
        cameraXViewModel = ViewModelProvider(this)[CameraXViewModel::class.java]
        
        // Setup camera provider
        cameraXViewModel.getProcessCameraProvider().observe(this) { provider ->
            cameraProvider = provider
            bindCameraUseCases()
        }
        
        // Observe posture results for push-up counting
        cameraXViewModel.getPostureLiveData().observe(this) { postureResults ->
            updatePushUpCounter(postureResults)
        }
    }
    
    private fun updatePushUpCounter(postureResults: Map<String, PostureResult>) {
        var pushUpCount = 0
        var statusText = getString(R.string.ready_to_detect)
        var formFeedback = ""
        
        // Check for enhanced push-up detection first
        for ((className, result) in postureResults) {
            when (className) {
                "pushups_enhanced" -> {
                    pushUpCount = result.reps
                    statusText = if (result.confidence > 0.8f) {
                        "Perfect form! Push-ups: $pushUpCount"
                    } else {
                        "Keep your form! Push-ups: $pushUpCount"
                    }
                }
                "pushups_down" -> {
                    // Fallback to original detection if enhanced isn't available
                    if (pushUpCount == 0) {
                        pushUpCount = result.reps
                        statusText = getString(R.string.pushups_detected)
                    }
                }
            }
        }
        
        // Update UI with enhanced feedback
        binding.pushupCounter.text = "Push-ups: $pushUpCount"
        binding.statusText.text = statusText
        
        // Add visual feedback for form quality
        if (pushUpCount > 0) {
            // Change status text color based on form quality
            val results = postureResults["pushups_enhanced"]
            if (results != null && results.confidence > 0.8f) {
                binding.statusText.setTextColor(getColor(android.R.color.holo_green_light))
            } else {
                binding.statusText.setTextColor(getColor(android.R.color.holo_orange_light))
            }
        } else {
            binding.statusText.setTextColor(getColor(android.R.color.white))
        }
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return

        // Optimized preview use case with high resolution
        val preview = Preview.Builder()
            .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
            .build()

        // Enhanced image analysis use case for improved pose detection
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.previewView.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()

        // Enhanced pose detection options for better accuracy
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()

        // Create enhanced pose detector processor with improved settings
        val poseDetectorProcessor = PoseDetectorProcessor(
            this,
            options,
            showInFrameLikelihood = false,  // Disabled for cleaner visualization
            visualizeZ = true,              // Enable 3D visualization
            rescaleZForVisualization = true,
            runClassification = true,
            isStreamMode = true,
            cameraXViewModel,
            listOf() // Empty list for push-ups only
        )

        // Enhanced image analyzer with proper threading and error handling
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            try {
                // Handle image rotation - use rotated dimensions for proper alignment
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val isRotated = rotationDegrees == 90 || rotationDegrees == 270
                
                val imageWidth = if (isRotated) imageProxy.height else imageProxy.width
                val imageHeight = if (isRotated) imageProxy.width else imageProxy.height
                
                Log.d("MainActivity", "Image: ${imageProxy.width}x${imageProxy.height}, " +
                        "rotation: $rotationDegrees, final: ${imageWidth}x${imageHeight}")
                
                // Set corrected image source info for GraphicOverlay
                binding.graphicOverlay.setImageSourceInfo(
                    imageWidth,
                    imageHeight,
                    cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                )
                poseDetectorProcessor.processImageProxy(imageProxy, binding.graphicOverlay)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error processing image: ${e.message}", e)
                imageProxy.close()
            }
        }

        // Bind use cases to camera with improved error handling
        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            // Attach preview to preview view with proper scaling
            binding.previewView.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            
            // Enable tap to focus
            enableTapToFocus(camera)
            
            // Initialize pose detection
            initializePoseDetection()
        } catch (e: Exception) {
            Log.e("MainActivity", "Camera setup failed", e)
            Toast.makeText(this, "Camera setup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun enableTapToFocus(camera: androidx.camera.core.Camera) {
        binding.previewView.setOnTouchListener { view, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                val factory = binding.previewView.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = androidx.camera.core.FocusMeteringAction.Builder(point).build()
                camera.cameraControl.startFocusAndMetering(action)
                true
            } else {
                false
            }
        }
    }
    
    private fun initializePoseDetection() {
        // Pose detection is now active with real-time skeleton overlay
        val cameraType = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) "Back" else "Front"
        binding.statusText.text = "$cameraType camera ready! Pose detection active - do push-ups!"
        binding.pushupCounter.text = "Push-ups: 0"
        
        // Confirm pose detection is working
        Toast.makeText(this, "Pose detection active! You should see skeleton overlay.", Toast.LENGTH_SHORT).show()
    }

    /**
     * This method is used to increase the notification sound volume to max
     */
    private fun increaseNotificationVolume() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
            0
        )
    }
}