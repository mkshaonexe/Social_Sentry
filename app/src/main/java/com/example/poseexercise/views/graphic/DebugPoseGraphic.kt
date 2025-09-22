/*
 * Debug Pose Graphic for testing coordinate alignment
 */

package com.example.poseexercise.views.graphic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Debug graphic to test pose coordinate alignment
 */
class DebugPoseGraphic(
    overlay: GraphicOverlay,
    private val pose: Pose
) : GraphicOverlay.Graphic(overlay) {

    private val debugPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 12f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 24f
        isAntiAlias = true
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }

    override fun draw(canvas: Canvas) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }

        // Draw key landmarks with coordinates for debugging
        val keyLandmarks = listOf(
            PoseLandmark.NOSE,
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_ELBOW,
            PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.LEFT_WRIST,
            PoseLandmark.RIGHT_WRIST
        )

        for (landmarkType in keyLandmarks) {
            val landmark = pose.getPoseLandmark(landmarkType)
            if (landmark != null && landmark.inFrameLikelihood > 0.5f) {
                val x = translateX(landmark.position.x)
                val y = translateY(landmark.position.y)
                
                // Draw big red dot
                canvas.drawCircle(x, y, 15f, debugPaint)
                
                // Draw coordinate text
                canvas.drawText(
                    String.format("(%.0f,%.0f)", landmark.position.x, landmark.position.y),
                    x + 20f,
                    y,
                    textPaint
                )
                
                Log.d("DebugPose", String.format(
                    "%s: raw(%.1f,%.1f) -> screen(%.1f,%.1f)", 
                    getLandmarkName(landmarkType),
                    landmark.position.x, landmark.position.y, x, y
                ))
            }
        }
        
        // Draw center cross for reference
        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f
        val crossSize = 50f
        
        canvas.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY, debugPaint)
        canvas.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize, debugPaint)
        
        canvas.drawText("CENTER", centerX + crossSize + 10f, centerY, textPaint)
    }
    
    private fun getLandmarkName(type: Int): String {
        return when (type) {
            PoseLandmark.NOSE -> "NOSE"
            PoseLandmark.LEFT_SHOULDER -> "L_SHOULDER"
            PoseLandmark.RIGHT_SHOULDER -> "R_SHOULDER"
            PoseLandmark.LEFT_ELBOW -> "L_ELBOW"
            PoseLandmark.RIGHT_ELBOW -> "R_ELBOW"
            PoseLandmark.LEFT_WRIST -> "L_WRIST"
            PoseLandmark.RIGHT_WRIST -> "R_WRIST"
            else -> "UNKNOWN"
        }
    }
}
