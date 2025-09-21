package com.example.socialsentry.service

import android.content.Context
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.PoseAccurateDetectorOptions
import kotlin.math.abs
import kotlin.math.sqrt

class PushUpDetector(private val context: Context) {
    
    private val poseDetector = PoseDetection.getClient(
        PoseAccurateDetectorOptions.Builder()
            .setDetectorMode(PoseAccurateDetectorOptions.STREAM_MODE)
            .build()
    )
    
    private var isInDownPosition = false
    private var pushUpCount = 0
    private var lastShoulderY = 0f
    private var lastElbowY = 0f
    private var lastWristY = 0f
    
    // Thresholds for push-up detection
    private val minDownThreshold = 0.15f // Minimum distance for down position
    private val minUpThreshold = 0.05f   // Minimum distance for up position
    private val stabilityThreshold = 0.02f // Stability threshold
    
    companion object {
        private const val TAG = "PushUpDetector"
    }
    
    fun detectPushUp(image: InputImage, onPushUpDetected: (Int) -> Unit) {
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                processPose(pose, onPushUpDetected)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Pose detection failed", e)
            }
    }
    
    private fun processPose(pose: Pose, onPushUpDetected: (Int) -> Unit) {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        
        // Check if we have all required landmarks
        if (leftShoulder == null || rightShoulder == null || 
            leftElbow == null || rightElbow == null || 
            leftWrist == null || rightWrist == null) {
            return
        }
        
        // Calculate average positions
        val avgShoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2
        val avgElbowY = (leftElbow.position.y + rightElbow.position.y) / 2
        val avgWristY = (leftWrist.position.y + rightWrist.position.y) / 2
        
        // Calculate arm angles to ensure proper push-up form
        val leftArmAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
        val rightArmAngle = calculateAngle(rightShoulder, rightElbow, rightWrist)
        
        // Check if arms are in proper push-up position (angles between 90-180 degrees)
        val isProperForm = leftArmAngle in 90f..180f && rightArmAngle in 90f..180f
        
        if (!isProperForm) {
            Log.d(TAG, "Improper form detected - angles: L:$leftArmAngle, R:$rightArmAngle")
            return
        }
        
        // Calculate vertical movement
        val shoulderMovement = if (lastShoulderY > 0) abs(avgShoulderY - lastShoulderY) else 0f
        val elbowMovement = if (lastElbowY > 0) abs(avgElbowY - lastElbowY) else 0f
        val wristMovement = if (lastWristY > 0) abs(avgWristY - lastWristY) else 0f
        
        // Check for stability (avoid counting during rapid movements)
        val isStable = shoulderMovement < stabilityThreshold && 
                      elbowMovement < stabilityThreshold && 
                      wristMovement < stabilityThreshold
        
        if (!isStable) {
            Log.d(TAG, "Movement too rapid - skipping detection")
            return
        }
        
        // Calculate distance between shoulders and wrists (push-up depth)
        val pushUpDepth = avgWristY - avgShoulderY
        
        Log.d(TAG, "Push-up depth: $pushUpDepth, isInDownPosition: $isInDownPosition")
        
        when {
            // Transition from up to down position
            !isInDownPosition && pushUpDepth > minDownThreshold -> {
                isInDownPosition = true
                Log.d(TAG, "Entered down position")
            }
            
            // Transition from down to up position (complete push-up)
            isInDownPosition && pushUpDepth < minUpThreshold -> {
                isInDownPosition = false
                pushUpCount++
                Log.d(TAG, "Push-up completed! Count: $pushUpCount")
                onPushUpDetected(pushUpCount)
            }
        }
        
        // Update last positions for next frame
        lastShoulderY = avgShoulderY
        lastElbowY = avgElbowY
        lastWristY = avgWristY
    }
    
    private fun calculateAngle(point1: PoseLandmark, point2: PoseLandmark, point3: PoseLandmark): Float {
        val vector1X = point1.position.x - point2.position.x
        val vector1Y = point1.position.y - point2.position.y
        val vector2X = point3.position.x - point2.position.x
        val vector2Y = point3.position.y - point2.position.y
        
        val dotProduct = vector1X * vector2X + vector1Y * vector2Y
        val magnitude1 = sqrt(vector1X * vector1X + vector1Y * vector1Y)
        val magnitude2 = sqrt(vector2X * vector2X + vector2Y * vector2Y)
        
        if (magnitude1 == 0f || magnitude2 == 0f) return 0f
        
        val cosAngle = dotProduct / (magnitude1 * magnitude2)
        val angleRadians = kotlin.math.acos(kotlin.math.max(-1f, kotlin.math.min(1f, cosAngle)))
        
        return Math.toDegrees(angleRadians.toDouble()).toFloat()
    }
    
    fun resetCounter() {
        pushUpCount = 0
        isInDownPosition = false
        lastShoulderY = 0f
        lastElbowY = 0f
        lastWristY = 0f
        Log.d(TAG, "Push-up counter reset")
    }
    
    fun getCurrentCount(): Int = pushUpCount
    
    fun close() {
        poseDetector.close()
    }
}
