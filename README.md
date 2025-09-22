# Android Push-up Detection App

## Overview
An advanced Android application that uses ML Kit Pose Detection to accurately count push-ups in real-time with form feedback and coaching.

## Features

### 🎯 **Enhanced Push-up Detection**
- **Angle-based Analysis**: Precise elbow and torso angle calculations
- **Form Quality Assessment**: Real-time feedback on push-up form
- **State Machine Logic**: Accurate counting with NEUTRAL → UP → DOWN states
- **Confidence Scoring**: Quality assessment for each repetition

### 📱 **Advanced Camera System**
- **High-resolution Processing**: Optimized camera configuration
- **Tap-to-Focus**: Touch-to-focus functionality for better clarity
- **Auto Camera Switching**: Front/back camera toggle
- **Enhanced Error Handling**: Robust camera operation management

### 🦴 **Real-time Skeleton Detection**
- **Enhanced Visualization**: Improved colors and anti-aliasing
- **3D Depth Rendering**: Z-axis visualization with color mapping
- **Key Joint Highlighting**: Different colors for important joints
- **Perfect Alignment**: Skeleton overlay precisely matches body position

### 🎨 **User Interface**
- **Real-time Form Feedback**: Visual coaching overlay
- **Color-coded Status**: Green for good form, red for issues
- **Performance Metrics**: Count display with form quality indicators
- **Reset Functionality**: Long-press counter to reset

## Technical Specifications

### Requirements
- **Android SDK**: Minimum API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **ML Kit**: Pose Detection API
- **CameraX**: Camera lifecycle management
- **Kotlin**: Primary development language

### Architecture
- **MVVM Pattern**: Clean architecture with ViewModels
- **Real-time Processing**: Efficient pose detection pipeline
- **Custom Graphics**: Hardware-accelerated skeleton rendering
- **State Management**: Robust exercise state tracking

## Key Improvements

### Camera & Detection Engine
```kotlin
// Enhanced camera configuration
val imageAnalysis = ImageAnalysis.Builder()
    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .setTargetRotation(binding.previewView.display.rotation)
    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
    .build()
```

### Advanced Push-up Algorithm
```java
// Precise angle calculations for push-up detection
private double calculateElbowAngle(PoseLandmark shoulder, PoseLandmark elbow, PoseLandmark wrist) {
    double dotProduct = vec1X * vec2X + vec1Y * vec2Y;
    double mag1 = Math.sqrt(vec1X * vec1X + vec1Y * vec1Y);
    double mag2 = Math.sqrt(vec2X * vec2X + vec2Y * vec2Y);
    return Math.toDegrees(Math.acos(dotProduct / (mag1 * mag2)));
}
```

### Perfect Skeleton Alignment
```java
// Enhanced coordinate transformation for precise overlay
if (imageAspectRatio > viewAspectRatio) {
    scaleFactor = viewHeight / imageHeight;
    postScaleWidthOffset = (imageWidth * scaleFactor - viewWidth) / 2;
} else {
    scaleFactor = viewWidth / imageWidth;
    postScaleHeightOffset = (imageHeight * scaleFactor - viewHeight) / 2;
}
```

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/mkshaonexe/android_pushup_detation.git
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build and Run**:
   - Sync project with Gradle files
   - Build the project
   - Run on device or emulator

## Usage

1. **Launch the app** and grant camera permissions
2. **Position yourself** in the camera view
3. **Get into push-up position** - the skeleton will appear
4. **Start doing push-ups** - the app will count automatically
5. **Follow form feedback** - green indicators show good form
6. **Reset if needed** - long-press the counter to reset

## Performance Features

- **Efficient Processing**: Optimized for real-time performance
- **Smart Buffering**: Reduces false positives with frame buffering
- **Memory Management**: Efficient resource usage
- **Hardware Acceleration**: GPU-accelerated graphics rendering

## Form Feedback System

- **Real-time Coaching**: Instant feedback on form quality
- **Visual Indicators**: Color-coded skeleton for form assessment
- **State Communication**: Clear UP/DOWN/NEUTRAL position feedback
- **Progress Tracking**: Accurate rep counting with confidence scores

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Google ML Kit**: For pose detection capabilities
- **CameraX**: For camera lifecycle management
- **Android Jetpack**: For modern Android development

## Contact

- **Developer**: [mkshaonexe](https://github.com/mkshaonexe)
- **Repository**: [android_pushup_detation](https://github.com/mkshaonexe/android_pushup_detation)

---

## Screenshots

*Add screenshots of the app in action showing skeleton detection and push-up counting*

## Demo Video

*Add link to demo video showing the app's functionality*

---

**Built with ❤️ for fitness enthusiasts who want accurate push-up tracking!**