@echo off
echo ========================================
echo Android Push-up Detection App - Build Script
echo ========================================
echo.

echo [1/4] Cleaning previous build...
call gradlew clean
if %errorlevel% neq 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo [2/4] Building debug APK...
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/4] Copying APK with new name...
copy "app\build\outputs\apk\debug\app-debug.apk" "push_up_count.apk"
if %errorlevel% neq 0 (
    echo ERROR: Copy failed!
    pause
    exit /b 1
)

echo.
echo [4/4] Creating documentation package...
mkdir docs 2>nul
copy "README.md" "docs\"
copy "CHANGELOG.md" "docs\"
copy "TECHNICAL_DOCUMENTATION.md" "docs\"
copy "CHANGES_SUMMARY.md" "docs\"

echo.
echo ========================================
echo BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.
echo Files created:
echo - push_up_count.apk (Ready to install!)
echo - docs\README.md
echo - docs\CHANGELOG.md
echo - docs\TECHNICAL_DOCUMENTATION.md
echo - docs\CHANGES_SUMMARY.md
echo.
echo APK Location: %cd%\push_up_count.apk
echo.
echo Ready for GitHub upload!
echo.
pause
