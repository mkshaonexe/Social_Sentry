/*
 * Push-up Form Feedback Overlay
 * Provides visual feedback for push-up form and technique
 */

package com.example.poseexercise.views.graphic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Graphic overlay for push-up form feedback
 */
class PushUpFormGraphic(
    overlay: GraphicOverlay,
    private val pose: Pose,
    private val pushUpCount: Int,
    private val currentState: String,
    private val formFeedback: String,
    private val isInCorrectPosition: Boolean
) : GraphicOverlay.Graphic(overlay) {

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val feedbackPaint = Paint().apply {
        color = Color.CYAN
        textSize = 36f
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val backgroundPaint = Paint().apply {
        color = Color.argb(120, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private val formIndicatorPaint = Paint().apply {
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        if (pose.allPoseLandmarks.isEmpty()) {
            return
        }

        // Create circular button like reference
        val buttonRadius = 50f
        val buttonX = canvas.width - buttonRadius - 30f // Position in bottom right like reference
        val buttonY = canvas.height - buttonRadius - 30f
        
        // Draw button background with rounded corners effect
        
        // Button background - solid blue like reference
        val buttonPaint = Paint().apply {
            color = Color.rgb(0, 120, 255) // Solid blue like reference
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(buttonX, buttonY, buttonRadius, buttonPaint)
        
        // Button border
        val borderPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawCircle(buttonX, buttonY, buttonRadius, borderPaint)

        // Draw push-up count in center of button
        val countText = "$pushUpCount"
        val countPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            isAntiAlias = true
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        
        canvas.drawText(
            countText,
            buttonX,
            buttonY + 16f, // +16f to center vertically in circle
            countPaint
        )

        // Minimal form indicators - only show if not in correct position
        if (!isInCorrectPosition) {
            drawMinimalFormIndicators(canvas)
        }
    }

    private fun drawMinimalFormIndicators(canvas: Canvas) {
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        // Set form indicator color - only red for incorrect form
        formIndicatorPaint.color = Color.RED
        formIndicatorPaint.strokeWidth = 6f

        // Draw simple circles at elbows to indicate incorrect form
        if (leftElbow != null && leftElbow.inFrameLikelihood > 0.5f) {
            canvas.drawCircle(
                translateX(leftElbow.position.x),
                translateY(leftElbow.position.y),
                25f,
                formIndicatorPaint
            )
        }
        
        if (rightElbow != null && rightElbow.inFrameLikelihood > 0.5f) {
            canvas.drawCircle(
                translateX(rightElbow.position.x),
                translateY(rightElbow.position.y),
                25f,
                formIndicatorPaint
            )
        }
    }

    // Removed old drawFormLine method as it's no longer needed
}
