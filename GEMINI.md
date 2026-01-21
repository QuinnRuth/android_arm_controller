# 🤖 Android Arm Controller - Project Memory

## 🏗️ Build System (Windows)
- **Status**: Fixed & Verified (2026-01-21)
- **Tool**: Gradle Wrapper (Gradle 8.9)
- **Command**: `.\gradlew.bat assembleDebug`
- **Output**: `app/build/outputs/apk/debug/app-debug.apk`

## 📝 Important Notes
- The project previously lacked the Windows Gradle Wrapper (`gradlew.bat`). It has been restored.
- Always prefer `.\gradlew.bat` over global `gradle` to ensure version compatibility.
- Local Build Environment: Scoop JDK 17 + Gradle Wrapper.

## 🔧 Troubleshooting
- If `gradlew.bat` permission denied: `git update-index --chmod=+x gradlew` (Linux/CI) or check properties.
- If `JAVA_HOME` issues: Ensure JDK 17 is active via Scoop (`scoop reset temurin17-jdk`).

## 📦 Dependencies
- **Kotlin**: 1.9.x
- **Compose**: Material 3
- **Navigation**: 2.7.7 (Added for Phase 2 UI)
- **BLE**: Nordic Android BLE Library 2.7.0
