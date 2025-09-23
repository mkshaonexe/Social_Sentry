# Technical Documentation

## Android Push-up Detection App - Technical Implementation

### 🏗️ **Architecture Overview**

The app follows a clean MVVM architecture with the following key components:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│  MainActivity.kt  │  PushUpFormGraphic.kt  │  PoseGraphic.kt │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    Business Logic Layer                     │
├─────────────────────────────────────────────────────────────┤
│  PoseDetectorProcessor.kt  │  ImprovedPushUpDetector.java   │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                               │
├─────────────────────────────────────────────────────────────┤
│  ML Kit Pose Detection  │  CameraX  │  GraphicOverlay.java  │
└─────────────────────────────────────────────────────────────┘
```

### 🔍 **Core Detection Algorithm**

#### **Mathematical Foundation**

The push-up detection is based on **elbow angle calculation** using vector mathematics:

```java
// 1. Extract landmark positions
float sx = shoulder.getPosition().x, sy = shoulder.getPosition().y;
float ex = elbow.getPosition().x, ey = elbow.getPosition().y;
float wx = wrist.getPosition().x, wy = wrist.getPosition().y;

// 2. Create vectors from elbow point
double v1x = sx - ex, v1y = sy - ey;  // Elbow to Shoulder
double v2x = wx - ex, v2y = wy - ey;  // Elbow to Wrist

// 3. Calculate angle using dot product
double dot = v1x * v2x + v1y * v2y;
double mag1 = Math.sqrt(v1x * v1x + v1y * v1y);
double mag2 = Math.sqrt(v2x * v2x + v2y * v2y);
double cosAngle = dot / (mag1 * mag2);
double angle = Math.toDegrees(Math.acos(cosAngle));
```

#### **State Machine Logic**

```java
public enum State { UNKNOWN, UP, DOWN }

// State determination based on elbow angle
private State determineState(double elbowAngle) {
    if (elbowAngle <= 70.0) {
        return State.DOWN;    // Arms bent (push-up down position)
    } else if (elbowAngle >= 130.0) {
        return State.UP;      // Arms extended (push-up up position)
    } else {
        return currentState;  // In-between, maintain current state
    }
}
```

#### **Counting Mechanism**

```java
// Push-up is counted on DOWN → UP transition
if (previousState == State.DOWN && currentState == State.UP) {
    pushUpCount++;
    Log.d(TAG, "PUSH-UP COMPLETED! Count: " + pushUpCount);
}
```

### 🎨 **Skeleton Rendering System**

#### **Coordinate Transformation**

The app uses a sophisticated coordinate transformation system to align the skeleton overlay with the camera preview:

```kotlin
// Calculate scale factors for proper alignment
if (imageAspectRatio > viewAspectRatio) {
    scaleFactor = viewHeight / imageHeight;
    postScaleWidthOffset = (imageWidth * scaleFactor - viewWidth) / 2;
} else {
    scaleFactor = viewWidth / imageWidth;
    postScaleHeightOffset = (imageHeight * scaleFactor - viewHeight) / 2;
}
```

#### **Skeleton Drawing Pipeline**

```kotlin
override fun draw(canvas: Canvas) {
    // 1. Filter out face landmarks
    val faceTypes = setOf(
        PoseLandmark.NOSE, PoseLandmark.LEFT_EYE_INNER, 
        PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EYE_OUTER,
        // ... other face landmarks
    )
    
    // 2. Draw only body landmarks
    for (landmark in landmarks) {
        if (landmark.landmarkType !in faceTypes) {
            drawJoint(canvas, landmark)
        }
    }
    
    // 3. Draw body connections
    drawBodyConnections(canvas)
}
```

#### **Joint Rendering**

```kotlin
private fun drawJoint(canvas: Canvas, landmark: PoseLandmark) {
    val point = landmark.position3D
    val cx = translateX(point.x)
    val cy = translateY(point.y)
    
    // Draw solid cyan circle
    canvas.drawCircle(cx, cy, JOINT_OUTER_RADIUS, jointFillPaint)
}
```

### 📱 **Camera Integration**

#### **CameraX Configuration**

```kotlin
val imageAnalysis = ImageAnalysis.Builder()
    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .setTargetRotation(binding.previewView.display.rotation)
    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
    .build()
```

#### **Frame Processing Pipeline**

```kotlin
override fun detectInImage(image: InputImage): Task<PoseWithClassification> {
    return detector.process(image)
        .continueWith(classificationExecutor) { task ->
            val pose = task.result
            val pushUpCount = improvedPushUpDetector.processPose(pose)
            // ... create result
        }
}
```

### ⚡ **Performance Optimizations**

#### **Frame Rate Control**

```kotlin
// Throttle processing to 20 FPS for optimal performance
private val minFrameInterval = 50L

if (currentTime - lastProcessTime < minFrameInterval && lastPoseResult != null) {
    // Use cached result for smooth display
    renderPoseResults(lastPoseResult!!, graphicOverlay)
    return
}
```

#### **Memory Management**

```kotlin
// Reuse paint objects to reduce GC pressure
private val jointFillPaint: Paint = Paint()
private val connectionPaint: Paint = Paint()

// Configure once in init block
init {
    jointFillPaint.apply {
        color = skeletonColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }
}
```

### 🔧 **Configuration Parameters**

#### **Detection Thresholds**

```java
// Optimized for accuracy vs strictness
private static final double MIN_ELBOW_ANGLE_DOWN = 70.0;  // Down position
private static final double MAX_ELBOW_ANGLE_UP = 130.0;   // Up position
private static final double MIN_CONFIDENCE = 0.7f;        // Landmark confidence
```

#### **Stability Settings**

```java
// Prevent false counts from jittery movements
private static final int MIN_FRAMES_FOR_STATE = 3;        // State confirmation
private static final int FRAME_BUFFER_SIZE = 5;           // Error tolerance
```

#### **Visual Settings**

```kotlin
// Skeleton appearance
private const val JOINT_OUTER_RADIUS = 10.0f
private const val STROKE_WIDTH = 7.0f
private val skeletonColor = Color.rgb(0, 255, 255)  // Bright cyan
```

### 🧪 **Testing & Validation**

#### **Unit Tests**

```java
@Test
public void testElbowAngleCalculation() {
    // Test with known coordinates
    PoseLandmark shoulder = createMockLandmark(0, 0);
    PoseLandmark elbow = createMockLandmark(0, 1);
    PoseLandmark wrist = createMockLandmark(1, 1);
    
    double angle = calculateElbowAngle(shoulder, elbow, wrist);
    assertEquals(90.0, angle, 0.1);
}
```

#### **Integration Tests**

```kotlin
@Test
fun testPushUpCounting() {
    val detector = ImprovedPushUpDetector()
    
    // Simulate DOWN state
    detector.processPose(createMockPose(60.0)) // Down position
    assertEquals(0, detector.getPushUpCount())
    
    // Simulate UP state (should count)
    detector.processPose(createMockPose(140.0)) // Up position
    assertEquals(1, detector.getPushUpCount())
}
```

### 📊 **Performance Monitoring**

#### **Key Metrics**

```kotlin
// Track detection performance
Log.d(TAG, "Elbow angle: ${elbowAngle}°, State: ${currentState}, Count: ${pushUpCount}")

// Monitor frame processing
val processingTime = System.currentTimeMillis() - startTime
if (processingTime > 50) {
    Log.w(TAG, "Slow frame processing: ${processingTime}ms")
}
```

#### **Memory Usage**

```kotlin
// Monitor memory usage
val runtime = Runtime.getRuntime()
val usedMemory = runtime.totalMemory() - runtime.freeMemory()
val maxMemory = runtime.maxMemory()
val memoryUsage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100
```

### 🚀 **Deployment & Distribution**

#### **Build Configuration**

```gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 21
        targetSdk 34
        versionCode 2
        versionName "2.0.0"
    }
    buildTypes {
        debug {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
    }
}
```

#### **APK Signing**

```gradle
signingConfigs {
    release {
        storeFile file('release-key.keystore')
        storePassword 'password'
        keyAlias 'pushup-app'
        keyPassword 'password'
    }
}
```

### 🔒 **Security Considerations**

#### **Permissions**

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
```

#### **Data Privacy**

- No personal data stored locally
- No network communication
- Camera data processed in real-time only
- No pose data persistence

### 🐛 **Troubleshooting**

#### **Common Issues**

1. **Low Detection Accuracy**
   - Ensure good lighting conditions
   - Position camera at appropriate distance
   - Check landmark confidence scores

2. **Performance Issues**
   - Reduce frame processing rate
   - Lower image resolution
   - Close other apps to free memory

3. **Skeleton Misalignment**
   - Recalibrate camera preview
   - Check device orientation
   - Verify coordinate transformation

#### **Debug Tools**

```kotlin
// Enable debug logging
Log.d("PushUpDetector", "Debug info: $debugInfo")

// Visual debugging
if (BuildConfig.DEBUG) {
    // Show additional debug overlays
    showDebugInfo(canvas)
}
```

---

**For more information, see [README.md](README.md) and [CHANGELOG.md](CHANGELOG.md)**
