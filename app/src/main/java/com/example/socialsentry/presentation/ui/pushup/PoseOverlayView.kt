package com.example.socialsentry.presentation.ui.pushup

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PoseOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pose: Pose? = null
    private var imageWidth = 1
    private var imageHeight = 1
    private var scaleFactor = 1f
    private var postScaleWidthOffset = 0f
    private var postScaleHeightOffset = 0f
    private var isFrontCamera = false
    
    // Smoothing variables
    private var smoothedPose: MutableMap<Int, PointF> = mutableMapOf()
    private val smoothingFactor = 0.7f // Higher = smoother but more lag

    private val paint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private val circlePaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
    }

    fun updatePose(pose: Pose?, imageWidth: Int, imageHeight: Int, isFrontCamera: Boolean) {
        this.pose = pose
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.isFrontCamera = isFrontCamera
        
        // Calculate proper scaling for ML Kit coordinates to view coordinates
        if (width > 0 && height > 0 && imageWidth > 0 && imageHeight > 0) {
            // ML Kit coordinates are in image space, we need to map to view space
            val scaleX = width.toFloat() / imageWidth.toFloat()
            val scaleY = height.toFloat() / imageHeight.toFloat()
            
            // Use uniform scaling to maintain aspect ratio
            scaleFactor = minOf(scaleX, scaleY)
            
            // Center the scaled image in the view
            val scaledImageWidth = imageWidth * scaleFactor
            val scaledImageHeight = imageHeight * scaleFactor
            postScaleWidthOffset = (width - scaledImageWidth) / 2f
            postScaleHeightOffset = (height - scaledImageHeight) / 2f
        }
        
        // Apply smoothing to pose landmarks
        pose?.let { currentPose ->
            smoothPoseLandmarks(currentPose)
        }
        
        invalidate()
    }
    
    private fun smoothPoseLandmarks(pose: Pose) {
        val landmarks = listOf(
            PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST,
            PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
            PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
        )
        
        landmarks.forEach { landmarkType ->
            pose.getPoseLandmark(landmarkType)?.let { landmark ->
                val currentPoint = PointF(landmark.position.x, landmark.position.y)
                val smoothedPoint = smoothedPose[landmarkType]
                
                if (smoothedPoint == null) {
                    // First time seeing this landmark
                    smoothedPose[landmarkType] = currentPoint
                } else {
                    // Apply exponential smoothing
                    smoothedPoint.x = smoothedPoint.x * smoothingFactor + currentPoint.x * (1 - smoothingFactor)
                    smoothedPoint.y = smoothedPoint.y * smoothingFactor + currentPoint.y * (1 - smoothingFactor)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val currentPose = pose ?: return
        
        // Draw skeleton connections
        drawLine(canvas, currentPose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)
        drawLine(canvas, currentPose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
        drawLine(canvas, currentPose, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        drawLine(canvas, currentPose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        drawLine(canvas, currentPose, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)
        
        drawLine(canvas, currentPose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP)
        drawLine(canvas, currentPose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP)
        drawLine(canvas, currentPose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
        
        drawLine(canvas, currentPose, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE)
        drawLine(canvas, currentPose, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)
        drawLine(canvas, currentPose, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE)
        drawLine(canvas, currentPose, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
        
        // Draw joints as circles
        drawCircle(canvas, currentPose, PoseLandmark.LEFT_SHOULDER)
        drawCircle(canvas, currentPose, PoseLandmark.RIGHT_SHOULDER)
        drawCircle(canvas, currentPose, PoseLandmark.LEFT_ELBOW)
        drawCircle(canvas, currentPose, PoseLandmark.RIGHT_ELBOW)
        drawCircle(canvas, currentPose, PoseLandmark.LEFT_WRIST)
        drawCircle(canvas, currentPose, PoseLandmark.RIGHT_WRIST)
        drawCircle(canvas, currentPose, PoseLandmark.LEFT_HIP)
        drawCircle(canvas, currentPose, PoseLandmark.RIGHT_HIP)
        drawCircle(canvas, currentPose, PoseLandmark.LEFT_KNEE)
        drawCircle(canvas, currentPose, PoseLandmark.RIGHT_KNEE)
        drawCircle(canvas, currentPose, PoseLandmark.LEFT_ANKLE)
        drawCircle(canvas, currentPose, PoseLandmark.RIGHT_ANKLE)
    }

    private fun drawLine(canvas: Canvas, pose: Pose, startLandmark: Int, endLandmark: Int) {
        val startSmoothed = smoothedPose[startLandmark] ?: return
        val endSmoothed = smoothedPose[endLandmark] ?: return
        
        val startX = translateX(startSmoothed.x)
        val startY = translateY(startSmoothed.y)
        val endX = translateX(endSmoothed.x)
        val endY = translateY(endSmoothed.y)
        
        canvas.drawLine(startX, startY, endX, endY, paint)
    }

    private fun drawCircle(canvas: Canvas, pose: Pose, landmark: Int) {
        val smoothedPoint = smoothedPose[landmark] ?: return
        val x = translateX(smoothedPoint.x)
        val y = translateY(smoothedPoint.y)
        canvas.drawCircle(x, y, 12f, circlePaint)
    }

    private fun translateX(x: Float): Float {
        val xScaled = x * scaleFactor + postScaleWidthOffset
        return if (isFrontCamera) width - xScaled else xScaled
    }

    private fun translateY(y: Float): Float {
        return y * scaleFactor + postScaleHeightOffset
    }
}
