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

package com.example.poseexercise.posedetector

import android.content.Context
import android.util.Log
import com.example.poseexercise.data.PostureResult
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor
import com.example.poseexercise.posedetector.classification.ImprovedPushUpDetector
import com.example.poseexercise.util.VisionProcessorBase
import com.example.poseexercise.viewmodels.CameraXViewModel
import com.example.poseexercise.views.graphic.GraphicOverlay
import com.example.poseexercise.views.graphic.PoseGraphic
import com.example.poseexercise.views.graphic.PushUpFormGraphic
import com.example.poseexercise.views.graphic.DebugPoseGraphic
import com.google.android.gms.tasks.Task
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/** A processor to run pose detector. */
class PoseDetectorProcessor(
    private val context: Context,
    options: PoseDetectorOptionsBase,
    private val showInFrameLikelihood: Boolean,
    private val visualizeZ: Boolean,
    private val rescaleZForVisualization: Boolean,
    private val runClassification: Boolean,
    private val isStreamMode: Boolean,
    private var cameraXViewModel: CameraXViewModel? = null,
    notCompletedExercise: List<String>
) : VisionProcessorBase<PoseDetectorProcessor.PoseWithClassification>(context) {

    private val detector: PoseDetector
    private val classificationExecutor: Executor

    private var poseClassifierProcessor: PoseClassifierProcessor? = null
    private var exercisesToDetect: List<String>? = null
    
    // Improved push-up detector with better counting
    private val improvedPushUpDetector = ImprovedPushUpDetector()
    
    // Smoothing variables  
    private var lastProcessTime = 0L
    private val minFrameInterval = 50L // Minimum 50ms between frames (20 FPS max for better detection)
    private var lastPoseResult: PoseWithClassification? = null

    /** Internal class to hold Pose and classification results. */
    inner class PoseWithClassification(
        val pose: Pose,
        val classificationResult: Map<String, PostureResult>
    ) {

        init {
            // update live data value
            if (classificationResult.isNotEmpty()) {
                cameraXViewModel?.postureLiveData?.postValue(classificationResult)
            }
        }
    }

    init {
        detector = PoseDetection.getClient(options)
        classificationExecutor = Executors.newSingleThreadExecutor()
        // Focus only on push-ups
        exercisesToDetect = listOf("Push up")
    }


    override fun stop() {
        super.stop()
        detector.close()
        cameraXViewModel = null
    }

    override fun detectInImage(image: InputImage): Task<PoseWithClassification> {
        return detector
            .process(image)
            .continueWith(
                classificationExecutor
            ) { task ->
                val pose = task.result
                val classificationResult: MutableMap<String, PostureResult> = HashMap()
                
                if (runClassification) {
                    // Use improved push-up detector
                    val pushUpCount = improvedPushUpDetector.processPose(pose)
                    val isInCorrectPosition = improvedPushUpDetector.isInCorrectPosition
                    
                    // Create improved result
                    val improvedResult = PostureResult(0, pushUpCount, 
                        if (isInCorrectPosition) 1.0f else 0.5f, "pushups_enhanced")
                    classificationResult["pushups_enhanced"] = improvedResult
                    
                    // Also run the original classifier for compatibility
                    if (poseClassifierProcessor == null) {
                        poseClassifierProcessor =
                            PoseClassifierProcessor(
                                context,
                                isStreamMode,
                                exercisesToDetect
                            )
                    }
                    val originalResults = poseClassifierProcessor!!.getPoseResult(pose)
                    classificationResult.putAll(originalResults)
                }
                
                PoseWithClassification(pose, classificationResult)
            }
    }

    override fun detectInImage(image: MlImage): Task<PoseWithClassification> {
        return detector
            .process(image)
            .continueWith(
                classificationExecutor
            ) { task ->
                val pose = task.result
                val classificationResult: MutableMap<String, PostureResult> = HashMap()
                
                if (runClassification) {
                    // Use improved push-up detector
                    val pushUpCount = improvedPushUpDetector.processPose(pose)
                    val isInCorrectPosition = improvedPushUpDetector.isInCorrectPosition
                    
                    // Create improved result
                    val improvedResult = PostureResult(0, pushUpCount, 
                        if (isInCorrectPosition) 1.0f else 0.5f, "pushups_enhanced")
                    classificationResult["pushups_enhanced"] = improvedResult
                    
                    // Also run the original classifier for compatibility
                    if (poseClassifierProcessor == null) {
                        poseClassifierProcessor =
                            PoseClassifierProcessor(
                                context,
                                isStreamMode,
                                exercisesToDetect
                            )
                    }
                    val originalResults = poseClassifierProcessor!!.getPoseResult(pose)
                    classificationResult.putAll(originalResults)
                }
                
                PoseWithClassification(pose, classificationResult)
            }
    }

    override fun onSuccess(
        poseWithClassification: PoseWithClassification,
        graphicOverlay: GraphicOverlay
    ) {
        // Implement frame rate throttling for smoother display
        val currentTime = System.currentTimeMillis()
        
        // Skip frames if they're coming too fast
        if (currentTime - lastProcessTime < minFrameInterval && lastPoseResult != null) {
            // Use the last result instead of the current one for smooth display
            renderPoseResults(lastPoseResult!!, graphicOverlay)
            return
        }
        
        lastProcessTime = currentTime
        lastPoseResult = poseWithClassification
        
        renderPoseResults(poseWithClassification, graphicOverlay)
    }
    
    private fun renderPoseResults(
        poseWithClassification: PoseWithClassification,
        graphicOverlay: GraphicOverlay
    ) {
        // Draw the basic skeleton
        graphicOverlay.add(
            PoseGraphic(
                graphicOverlay,
                poseWithClassification.pose,
                showInFrameLikelihood,
                visualizeZ,
                rescaleZForVisualization
            )
        )
        
        // Add push-up form feedback overlay
        val enhancedResult = poseWithClassification.classificationResult["pushups_enhanced"]
        if (enhancedResult != null) {
            graphicOverlay.add(
                PushUpFormGraphic(
                    graphicOverlay,
                    poseWithClassification.pose,
                    enhancedResult.reps,
                    improvedPushUpDetector.currentStateString,
                    improvedPushUpDetector.formFeedback,
                    improvedPushUpDetector.isInCorrectPosition
                )
            )
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Pose detection failed!", e)
    }

    override fun isMlImageEnabled(context: Context?): Boolean {
        // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
        return true
    }

    companion object {
        private const val TAG = "PoseDetectorProcessor"
    }
}
