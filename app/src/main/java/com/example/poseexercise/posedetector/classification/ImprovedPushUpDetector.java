/*
 * Improved Push-up Detection Algorithm
 * More accurate counting with simplified state machine
 */

package com.example.poseexercise.posedetector.classification;

import android.util.Log;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

/**
 * Improved push-up detection with better counting accuracy
 */
public class ImprovedPushUpDetector {
    private static final String TAG = "ImprovedPushUpDetector";
    
    // More lenient thresholds for better detection
    private static final double MIN_ELBOW_ANGLE_DOWN = 70.0; // Less strict down position
    private static final double MAX_ELBOW_ANGLE_UP = 130.0;  // Less strict up position
    private static final double MIN_CONFIDENCE = 0.7f; // Minimum landmark confidence
    
    // Simple state tracking
    private enum State {
        UNKNOWN, UP, DOWN
    }
    
    private State currentState = State.UNKNOWN;
    private State previousState = State.UNKNOWN;
    private int pushUpCount = 0;
    private int consecutiveFrames = 0;
    private static final int MIN_FRAMES_FOR_STATE = 3; // Minimum frames to confirm state
    
    // For debugging and feedback
    private double lastElbowAngle = 0.0;
    private boolean isInGoodForm = false;
    
    /**
     * Process pose and detect push-ups
     */
    public int processPose(Pose pose) {
        if (pose == null || pose.getAllPoseLandmarks().isEmpty()) {
            return pushUpCount;
        }
        
        // Get key landmarks
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        
        // Check if we have required landmarks with good confidence
        if (!hasValidLandmarks(leftShoulder, rightShoulder, leftElbow, rightElbow, leftWrist, rightWrist)) {
            return pushUpCount;
        }
        
        // Calculate average elbow angle
        double leftElbowAngle = calculateElbowAngle(leftShoulder, leftElbow, leftWrist);
        double rightElbowAngle = calculateElbowAngle(rightShoulder, rightElbow, rightWrist);
        double avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2.0;
        
        lastElbowAngle = avgElbowAngle;
        
        // Determine current state based on elbow angle
        State detectedState = determineState(avgElbowAngle);
        
        // Update state with stability check
        updateState(detectedState);
        
        Log.d(TAG, String.format("Elbow: %.1f°, State: %s->%s, Frames: %d, Count: %d", 
                avgElbowAngle, previousState, currentState, consecutiveFrames, pushUpCount));
        
        return pushUpCount;
    }
    
    /**
     * Check if landmarks are valid and confident
     */
    private boolean hasValidLandmarks(PoseLandmark... landmarks) {
        for (PoseLandmark landmark : landmarks) {
            if (landmark == null || landmark.getInFrameLikelihood() < MIN_CONFIDENCE) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calculate elbow angle using three points
     */
    private double calculateElbowAngle(PoseLandmark shoulder, PoseLandmark elbow, PoseLandmark wrist) {
        if (shoulder == null || elbow == null || wrist == null) {
            return 180.0;
        }
        
        // Get positions
        float sx = shoulder.getPosition().x, sy = shoulder.getPosition().y;
        float ex = elbow.getPosition().x, ey = elbow.getPosition().y;
        float wx = wrist.getPosition().x, wy = wrist.getPosition().y;
        
        // Create vectors from elbow
        double v1x = sx - ex, v1y = sy - ey;
        double v2x = wx - ex, v2y = wy - ey;
        
        // Calculate angle using dot product
        double dot = v1x * v2x + v1y * v2y;
        double mag1 = Math.sqrt(v1x * v1x + v1y * v1y);
        double mag2 = Math.sqrt(v2x * v2x + v2y * v2y);
        
        if (mag1 == 0 || mag2 == 0) return 180.0;
        
        double cosAngle = dot / (mag1 * mag2);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle)); // Clamp
        
        return Math.toDegrees(Math.acos(cosAngle));
    }
    
    /**
     * Determine state based on elbow angle
     */
    private State determineState(double elbowAngle) {
        if (elbowAngle <= MIN_ELBOW_ANGLE_DOWN) {
            isInGoodForm = true;
            return State.DOWN;
        } else if (elbowAngle >= MAX_ELBOW_ANGLE_UP) {
            isInGoodForm = true;
            return State.UP;
        } else {
            // In between - keep current state
            return currentState;
        }
    }
    
    /**
     * Update state with stability checking
     */
    private void updateState(State detectedState) {
        if (detectedState == currentState) {
            // Same state, increase stability counter
            consecutiveFrames++;
        } else {
            // State change detected
            if (consecutiveFrames >= MIN_FRAMES_FOR_STATE) {
                // Current state was stable, now change
                previousState = currentState;
                currentState = detectedState;
                consecutiveFrames = 1;
                
                // Check for push-up completion (DOWN -> UP transition)
                if (previousState == State.DOWN && currentState == State.UP) {
                    pushUpCount++;
                    Log.d(TAG, "PUSH-UP COMPLETED! Count: " + pushUpCount);
                }
            } else {
                // Not stable enough, just increment counter
                consecutiveFrames++;
            }
        }
    }
    
    /**
     * Reset counter and state
     */
    public void reset() {
        pushUpCount = 0;
        currentState = State.UNKNOWN;
        previousState = State.UNKNOWN;
        consecutiveFrames = 0;
        lastElbowAngle = 0.0;
        isInGoodForm = false;
    }
    
    /**
     * Get current count
     */
    public int getPushUpCount() {
        return pushUpCount;
    }
    
    /**
     * Check if in correct position
     */
    public boolean isInCorrectPosition() {
        return isInGoodForm && (currentState == State.UP || currentState == State.DOWN);
    }
    
    /**
     * Get current state string
     */
    public String getCurrentStateString() {
        switch (currentState) {
            case UP:
                return "UP Position - Go down";
            case DOWN:
                return "DOWN Position - Push up!";
            case UNKNOWN:
            default:
                return "Get in position";
        }
    }
    
    /**
     * Get form feedback
     */
    public String getFormFeedback() {
        if (!isInGoodForm) {
            return "Adjust your form";
        }
        
        switch (currentState) {
            case UP:
                return "Good! Now go down";
            case DOWN:
                return "Perfect! Now push up";
            case UNKNOWN:
            default:
                return "Start push-ups";
        }
    }
    
    /**
     * Get last measured elbow angle for debugging
     */
    public double getLastElbowAngle() {
        return lastElbowAngle;
    }
}
