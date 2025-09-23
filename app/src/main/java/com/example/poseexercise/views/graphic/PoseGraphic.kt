/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.poseexercise.views.graphic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.poseexercise.views.graphic.GraphicOverlay.Graphic
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.util.Locale

/** Draw the detected pose in preview. */
class PoseGraphic
internal constructor(
    overlay: GraphicOverlay,
    private val pose: Pose,
    private val showInFrameLikelihood: Boolean,
    private val visualizeZ: Boolean,
    private val rescaleZForVisualization: Boolean
) : Graphic(overlay) {
    private var zMin = java.lang.Float.MAX_VALUE
    private var zMax = java.lang.Float.MIN_VALUE
    private val leftPaint: Paint = Paint()
    private val rightPaint: Paint = Paint()
    private val whitePaint: Paint = Paint()
    private val jointFillPaint: Paint = Paint()
    private val jointStrokePaint: Paint = Paint()
    private val connectionPaint: Paint = Paint()

    init {
        // Enhanced paint settings for better visibility
        whitePaint.apply {
            strokeWidth = STROKE_WIDTH
            color = Color.WHITE
            textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        
        // Use exact cyan/turquoise color to match reference image
        val skeletonColor = Color.rgb(0, 255, 255) // Bright cyan exactly like reference
        val jointCenterColor = skeletonColor // Make joints same color as lines
        
        leftPaint.apply {
            strokeWidth = STROKE_WIDTH
            color = skeletonColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        
        rightPaint.apply {
            strokeWidth = STROKE_WIDTH
            color = skeletonColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        
        // Joint paints: solid cyan circles like reference
        jointFillPaint.apply {
            color = skeletonColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        jointStrokePaint.apply {
            color = skeletonColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH
        }
        
        // Paint for main body connections (cyan)
        connectionPaint.apply {
            strokeWidth = STROKE_WIDTH * 1.5f
            color = skeletonColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun draw(canvas: Canvas) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }

        // Draw only body joint points (exclude all face landmarks)
        val faceTypes = setOf(
            PoseLandmark.NOSE,
            PoseLandmark.LEFT_EYE_INNER, PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EYE_OUTER,
            PoseLandmark.RIGHT_EYE_INNER, PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EYE_OUTER,
            PoseLandmark.LEFT_EAR, PoseLandmark.RIGHT_EAR,
            PoseLandmark.LEFT_MOUTH, PoseLandmark.RIGHT_MOUTH
        )
        
        for (landmark in landmarks) {
            // Skip all face landmarks
            if (landmark.landmarkType !in faceTypes) {
                drawJoint(canvas, landmark)
                if (visualizeZ && rescaleZForVisualization) {
                    zMin = kotlin.math.min(zMin, landmark.position3D.z)
                    zMax = kotlin.math.max(zMax, landmark.position3D.z)
                }
            }
        }

        // Face landmarks removed on purpose for cleaner UI

        // Get all body landmarks (exclude face)

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
        val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
        val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
        val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
        val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
        val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
        val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
        val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
        val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
        val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

        // No face connections - completely removed

        // Body skeleton only (no face parts)
        // Main body structure
        drawLine(canvas, leftShoulder, rightShoulder, connectionPaint)
        drawLine(canvas, leftHip, rightHip, connectionPaint)
        
        // Left side of body
        drawLine(canvas, leftShoulder, leftElbow, leftPaint)
        drawLine(canvas, leftElbow, leftWrist, leftPaint)
        drawLine(canvas, leftShoulder, leftHip, leftPaint)
        drawLine(canvas, leftHip, leftKnee, leftPaint)
        drawLine(canvas, leftKnee, leftAnkle, leftPaint)
        drawLine(canvas, leftWrist, leftThumb, leftPaint)
        drawLine(canvas, leftWrist, leftPinky, leftPaint)
        drawLine(canvas, leftWrist, leftIndex, leftPaint)
        drawLine(canvas, leftIndex, leftPinky, leftPaint)
        drawLine(canvas, leftAnkle, leftHeel, leftPaint)
        drawLine(canvas, leftHeel, leftFootIndex, leftPaint)

        // Right side of body
        drawLine(canvas, rightShoulder, rightElbow, rightPaint)
        drawLine(canvas, rightElbow, rightWrist, rightPaint)
        drawLine(canvas, rightShoulder, rightHip, rightPaint)
        drawLine(canvas, rightHip, rightKnee, rightPaint)
        drawLine(canvas, rightKnee, rightAnkle, rightPaint)
        drawLine(canvas, rightWrist, rightThumb, rightPaint)
        drawLine(canvas, rightWrist, rightPinky, rightPaint)
        drawLine(canvas, rightWrist, rightIndex, rightPaint)
        drawLine(canvas, rightIndex, rightPinky, rightPaint)
        drawLine(canvas, rightAnkle, rightHeel, rightPaint)
        drawLine(canvas, rightHeel, rightFootIndex, rightPaint)

        // Draw inFrameLikelihood for all points
        if (showInFrameLikelihood) {
            for (landmark in landmarks) {
                canvas.drawText(
                    String.format(Locale.US, "%.2f", landmark.inFrameLikelihood),
                    translateX(landmark.position.x),
                    translateY(landmark.position.y),
                    whitePaint
                )
            }
        }
    }

    private fun drawJoint(canvas: Canvas, landmark: PoseLandmark) {
        val point = landmark.position3D
        // Solid cyan circle like reference
        updatePaintColorByZValue(
            jointFillPaint,
            canvas,
            visualizeZ,
            rescaleZForVisualization,
            point.z,
            zMin,
            zMax
        )
        val cx = translateX(point.x)
        val cy = translateY(point.y)
        canvas.drawCircle(cx, cy, JOINT_OUTER_RADIUS, jointFillPaint)
    }

    private fun drawLine(
        canvas: Canvas,
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?,
        paint: Paint
    ) {
        val start = startLandmark!!.position3D
        val end = endLandmark!!.position3D

        // Gets average z for the current body line
        val avgZInImagePixel = (start.z + end.z) / 2
        updatePaintColorByZValue(
            paint,
            canvas,
            visualizeZ,
            rescaleZForVisualization,
            avgZInImagePixel,
            zMin,
            zMax
        )

        canvas.drawLine(
            translateX(start.x),
            translateY(start.y),
            translateX(end.x),
            translateY(end.y),
            paint
        )
    }

    companion object {
        private const val JOINT_OUTER_RADIUS = 10.0f
        private const val JOINT_INNER_RADIUS = 5.0f
        private const val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f  // Increased text size
        private const val STROKE_WIDTH = 7.0f  // Slightly thicker like reference
    }
}
