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

        // Draw background for text
        val backgroundRect = Rect(20, 20, canvas.width - 20, 200)
        canvas.drawRect(backgroundRect, backgroundPaint)

        // Draw push-up count
        canvas.drawText(
            "Push-ups: $pushUpCount",
            40f,
            80f,
            textPaint
        )

        // Draw current state
        val statePaint = Paint(textPaint).apply {
            color = when {
                currentState.contains("UP") -> Color.GREEN
                currentState.contains("DOWN") -> Color.YELLOW
                else -> Color.WHITE
            }
        }
        canvas.drawText(
            currentState,
            40f,
            130f,
            statePaint
        )

        // Draw form feedback
        canvas.drawText(
            formFeedback,
            40f,
            180f,
            feedbackPaint
        )

        // Draw form indicators on the pose
        drawFormIndicators(canvas)
    }

    private fun drawFormIndicators(canvas: Canvas) {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        // Set form indicator color based on correctness
        formIndicatorPaint.color = if (isInCorrectPosition) {
            Color.GREEN
        } else {
            Color.RED
        }

        // Draw elbow form indicators
        if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            drawFormLine(canvas, leftShoulder, leftElbow, leftWrist)
        }
        if (rightShoulder != null && rightElbow != null && rightWrist != null) {
            drawFormLine(canvas, rightShoulder, rightElbow, rightWrist)
        }

        // Draw body alignment indicator (spine)
        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
            val shoulderMidX = (leftShoulder.position.x + rightShoulder.position.x) / 2f
            val shoulderMidY = (leftShoulder.position.y + rightShoulder.position.y) / 2f
            val hipMidX = (leftHip.position.x + rightHip.position.x) / 2f
            val hipMidY = (leftHip.position.y + rightHip.position.y) / 2f

            canvas.drawLine(
                translateX(shoulderMidX),
                translateY(shoulderMidY),
                translateX(hipMidX),
                translateY(hipMidY),
                formIndicatorPaint
            )
        }
    }

    private fun drawFormLine(
        canvas: Canvas,
        shoulder: PoseLandmark,
        elbow: PoseLandmark,
        wrist: PoseLandmark
    ) {
        // Draw shoulder to elbow
        canvas.drawLine(
            translateX(shoulder.position.x),
            translateY(shoulder.position.y),
            translateX(elbow.position.x),
            translateY(elbow.position.y),
            formIndicatorPaint
        )

        // Draw elbow to wrist
        canvas.drawLine(
            translateX(elbow.position.x),
            translateY(elbow.position.y),
            translateX(wrist.position.x),
            translateY(wrist.position.y),
            formIndicatorPaint
        )

        // Draw angle indicator at elbow
        canvas.drawCircle(
            translateX(elbow.position.x),
            translateY(elbow.position.y),
            20f,
            formIndicatorPaint
        )
    }
}
