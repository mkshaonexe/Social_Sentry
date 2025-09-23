package com.example.socialsentry.presentation.ui.pushup

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class ImprovedPushUpDetector {
    private enum class State { UNKNOWN, UP, DOWN }

    private var previousStableState: State = State.UNKNOWN
    private var candidateState: State = State.UNKNOWN
    private var candidateFrames: Int = 0
    private var pushUpCount: Int = 0

    private val minElbowAngleDown = 70.0
    private val maxElbowAngleUp = 130.0
    private val stabilityFrames = 3

    fun onPose(pose: Pose): Int {
        val leftAngle = elbowAngleDegrees(pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        val rightAngle = elbowAngleDegrees(pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)

        val angle = when {
            leftAngle != null && rightAngle != null -> (leftAngle + rightAngle) / 2.0
            leftAngle != null -> leftAngle
            rightAngle != null -> rightAngle
            else -> null
        }

        val currentState = when {
            angle == null -> previousStableState
            angle <= minElbowAngleDown -> State.DOWN
            angle >= maxElbowAngleUp -> State.UP
            else -> previousStableState
        }

        if (currentState == candidateState) {
            candidateFrames++
        } else {
            candidateState = currentState
            candidateFrames = 1
        }

        if (candidateFrames >= stabilityFrames && currentState != previousStableState) {
            if (previousStableState == State.DOWN && currentState == State.UP) {
                pushUpCount++
            }
            previousStableState = currentState
        }

        return pushUpCount
    }

    private fun elbowAngleDegrees(pose: Pose, shoulderType: Int, elbowType: Int, wristType: Int): Double? {
        val shoulder = pose.getPoseLandmark(shoulderType)
        val elbow = pose.getPoseLandmark(elbowType)
        val wrist = pose.getPoseLandmark(wristType)
        if (shoulder == null || elbow == null || wrist == null) return null

        val v1x = shoulder.position.x - elbow.position.x
        val v1y = shoulder.position.y - elbow.position.y
        val v2x = wrist.position.x - elbow.position.x
        val v2y = wrist.position.y - elbow.position.y

        val dot = v1x * v2x + v1y * v2y
        val mag1 = sqrt(v1x.toDouble().pow(2) + v1y.toDouble().pow(2))
        val mag2 = sqrt(v2x.toDouble().pow(2) + v2y.toDouble().pow(2))
        if (mag1 == 0.0 || mag2 == 0.0) return null
        val cos = (dot / (mag1 * mag2)).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cos))
    }
}


