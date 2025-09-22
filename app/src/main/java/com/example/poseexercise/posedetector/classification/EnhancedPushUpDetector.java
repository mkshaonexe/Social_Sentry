/*
 * Enhanced Push-up Detection Algorithm
 * Provides more accurate push-up counting with pose angle analysis
 */

package com.example.poseexercise.posedetector.classification;

import android.util.Log;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import java.util.List;

/**
 * Enhanced push-up detection algorithm using pose landmarks and angle calculations
 */
public class EnhancedPushUpDetector {
    private static final String TAG = "EnhancedPushUpDetector";
    
    // Thresholds for push-up detection
    private static final double MIN_ELBOW_ANGLE_DOWN = 60.0; // Minimum angle for down position
    private static final double MAX_ELBOW_ANGLE_UP = 140.0;  // Maximum angle for up position
    private static final double MIN_TORSO_STRAIGHTNESS = 150.0; // Minimum angle for straight torso
    private static final int FRAME_BUFFER_SIZE = 5; // Frames to buffer for stability
    
    // State tracking
    private enum PushUpState {
        NEUTRAL, DOWN_POSITION, UP_POSITION
    }
    
    private PushUpState currentState = PushUpState.NEUTRAL;
    private int pushUpCount = 0;
    private int frameBuffer = 0;
    private boolean isInCorrectPosition = false;
    private double lastElbowAngle = 0.0;
    private double lastTorsoAngle = 0.0;
    
    /**
     * Process a pose and update push-up count
     * @param pose The detected pose
     * @return Current push-up count
     */
    public int processPose(Pose pose) {
        if (pose == null || pose.getAllPoseLandmarks().isEmpty()) {
            return pushUpCount;
        }
        
        // Get key landmarks for push-up detection
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        
        // Check if we have all required landmarks
        if (!hasRequiredLandmarks(leftShoulder, rightShoulder, leftElbow, rightElbow, 
                                 leftWrist, rightWrist, leftHip, rightHip)) {
            return pushUpCount;
        }
        
        // Calculate angles
        double leftElbowAngle = calculateElbowAngle(leftShoulder, leftElbow, leftWrist);
        double rightElbowAngle = calculateElbowAngle(rightShoulder, rightElbow, rightWrist);
        double avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2.0;
        
        double torsoAngle = calculateTorsoAngle(leftShoulder, rightShoulder, leftHip, rightHip);
        
        // Update state based on angles
        updatePushUpState(avgElbowAngle, torsoAngle);
        
        lastElbowAngle = avgElbowAngle;
        lastTorsoAngle = torsoAngle;
        
        Log.d(TAG, String.format("Elbow angle: %.1f, Torso angle: %.1f, State: %s, Count: %d", 
                avgElbowAngle, torsoAngle, currentState, pushUpCount));
        
        return pushUpCount;
    }
    
    /**
     * Check if all required landmarks are detected with sufficient confidence
     */
    private boolean hasRequiredLandmarks(PoseLandmark... landmarks) {
        for (PoseLandmark landmark : landmarks) {
            if (landmark == null || landmark.getInFrameLikelihood() < 0.5f) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calculate elbow angle (shoulder-elbow-wrist)
     */
    private double calculateElbowAngle(PoseLandmark shoulder, PoseLandmark elbow, PoseLandmark wrist) {
        if (shoulder == null || elbow == null || wrist == null) {
            return 180.0; // Default to extended position
        }
        
        double shoulderX = shoulder.getPosition().x;
        double shoulderY = shoulder.getPosition().y;
        double elbowX = elbow.getPosition().x;
        double elbowY = elbow.getPosition().y;
        double wristX = wrist.getPosition().x;
        double wristY = wrist.getPosition().y;
        
        // Calculate vectors
        double vec1X = shoulderX - elbowX;
        double vec1Y = shoulderY - elbowY;
        double vec2X = wristX - elbowX;
        double vec2Y = wristY - elbowY;
        
        // Calculate angle using dot product
        double dotProduct = vec1X * vec2X + vec1Y * vec2Y;
        double mag1 = Math.sqrt(vec1X * vec1X + vec1Y * vec1Y);
        double mag2 = Math.sqrt(vec2X * vec2X + vec2Y * vec2Y);
        
        if (mag1 == 0 || mag2 == 0) {
            return 180.0;
        }
        
        double cosAngle = dotProduct / (mag1 * mag2);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle)); // Clamp to valid range
        
        return Math.toDegrees(Math.acos(cosAngle));
    }
    
    /**
     * Calculate torso straightness (shoulder-hip alignment)
     */
    private double calculateTorsoAngle(PoseLandmark leftShoulder, PoseLandmark rightShoulder, 
                                      PoseLandmark leftHip, PoseLandmark rightHip) {
        if (leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null) {
            return 180.0; // Default to straight
        }
        
        // Calculate midpoints
        double shoulderMidX = (leftShoulder.getPosition().x + rightShoulder.getPosition().x) / 2.0;
        double shoulderMidY = (leftShoulder.getPosition().y + rightShoulder.getPosition().y) / 2.0;
        double hipMidX = (leftHip.getPosition().x + rightHip.getPosition().x) / 2.0;
        double hipMidY = (leftHip.getPosition().y + rightHip.getPosition().y) / 2.0;
        
        // Calculate angle from vertical
        double deltaX = Math.abs(shoulderMidX - hipMidX);
        double deltaY = Math.abs(shoulderMidY - hipMidY);
        
        if (deltaY == 0) {
            return 90.0; // Horizontal
        }
        
        double angle = Math.toDegrees(Math.atan(deltaX / deltaY));
        return 180.0 - angle; // Convert to straightness measure
    }
    
    /**
     * Update push-up state based on calculated angles
     */
    private void updatePushUpState(double elbowAngle, double torsoAngle) {
        boolean torsoStraight = torsoAngle >= MIN_TORSO_STRAIGHTNESS;
        
        switch (currentState) {
            case NEUTRAL:
                if (torsoStraight && elbowAngle >= MAX_ELBOW_ANGLE_UP) {
                    currentState = PushUpState.UP_POSITION;
                    frameBuffer = 0;
                    isInCorrectPosition = true;
                }
                break;
                
            case UP_POSITION:
                if (torsoStraight && elbowAngle <= MIN_ELBOW_ANGLE_DOWN) {
                    currentState = PushUpState.DOWN_POSITION;
                    frameBuffer = 0;
                } else if (!torsoStraight || elbowAngle < MAX_ELBOW_ANGLE_UP - 20) {
                    // Lost proper form
                    frameBuffer++;
                    if (frameBuffer >= FRAME_BUFFER_SIZE) {
                        currentState = PushUpState.NEUTRAL;
                        isInCorrectPosition = false;
                    }
                }
                break;
                
            case DOWN_POSITION:
                if (torsoStraight && elbowAngle >= MAX_ELBOW_ANGLE_UP) {
                    // Completed a push-up!
                    pushUpCount++;
                    currentState = PushUpState.UP_POSITION;
                    frameBuffer = 0;
                } else if (!torsoStraight || elbowAngle > MIN_ELBOW_ANGLE_DOWN + 20) {
                    // Lost proper form
                    frameBuffer++;
                    if (frameBuffer >= FRAME_BUFFER_SIZE) {
                        currentState = PushUpState.NEUTRAL;
                        isInCorrectPosition = false;
                    }
                }
                break;
        }
    }
    
    /**
     * Reset the push-up counter
     */
    public void reset() {
        pushUpCount = 0;
        currentState = PushUpState.NEUTRAL;
        frameBuffer = 0;
        isInCorrectPosition = false;
        lastElbowAngle = 0.0;
        lastTorsoAngle = 0.0;
    }
    
    /**
     * Get current push-up count
     */
    public int getPushUpCount() {
        return pushUpCount;
    }
    
    /**
     * Check if user is in correct position for push-ups
     */
    public boolean isInCorrectPosition() {
        return isInCorrectPosition;
    }
    
    /**
     * Get current state for UI feedback
     */
    public String getCurrentStateString() {
        switch (currentState) {
            case UP_POSITION:
                return "UP - Ready to go down";
            case DOWN_POSITION:
                return "DOWN - Push up!";
            case NEUTRAL:
            default:
                return "Get in push-up position";
        }
    }
    
    /**
     * Get feedback based on current pose
     */
    public String getFormFeedback() {
        if (lastTorsoAngle < MIN_TORSO_STRAIGHTNESS) {
            return "Keep your body straight!";
        }
        
        switch (currentState) {
            case UP_POSITION:
                return "Great form! Now go down";
            case DOWN_POSITION:
                return "Good! Now push up";
            case NEUTRAL:
            default:
                return "Get into push-up position";
        }
    }
}
