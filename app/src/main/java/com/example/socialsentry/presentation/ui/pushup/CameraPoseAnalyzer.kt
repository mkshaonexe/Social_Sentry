package com.example.socialsentry.presentation.ui.pushup

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class CameraPoseAnalyzer(
    private val onPoseResult: (Pose, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: PoseDetector by lazy {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage: Image? = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                // Provide dimensions in the same orientation as the processed image
                val rotatedWidth = if (rotationDegrees == 90 || rotationDegrees == 270) mediaImage.height else mediaImage.width
                val rotatedHeight = if (rotationDegrees == 90 || rotationDegrees == 270) mediaImage.width else mediaImage.height
                onPoseResult(pose, rotatedWidth, rotatedHeight)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}


