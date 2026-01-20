# 六轴机械臂蓝牙控制器

一个基于 **Kotlin + Jetpack Compose** 的 Android 应用，用于通过蓝牙 SPP 控制六轴机械臂。

## 功能
- ✅ 蓝牙 SPP 连接 (HC-05/HC-06)
- ✅ 6 轴独立控制 (500-2500 PWM)
- ✅ 急停按钮 (发送 DISARM)
- ✅ 复位按钮 (所有轴回中位 1500)
- ✅ ARM 启动命令
- ✅ Material 3 现代 UI
- ✅ 适配 Android 12+ 权限

## 通信协议
与 Arduino 端使用相同的协议：
- 单轴控制: `#1P1500T50!` (轴1, PWM 1500, 时间50ms)
- 急停: `DISARM`
- 启动: `ARM`

## 编译

### 方式1: Android Studio
1. 用 Android Studio 打开此项目
2. 点击 Run

### 方式2: 命令行 (Gradle)
```bash
# 设置 Android SDK 路径
export ANDROID_HOME=/path/to/android/sdk

# 编译 Debug APK
./gradlew assembleDebug

# 产物位置
# app/build/outputs/apk/debug/app-debug.apk
```

### 方式3: GitHub Actions
Push 到 GitHub 后自动编译，从 Releases 下载 APK。

## 硬件要求
- HC-05 或 HC-06 蓝牙模块
- Arduino + 机械臂 (运行 `arduino_robot_arm.ino`)

## 权限
- BLUETOOTH_CONNECT (Android 12+)
- BLUETOOTH_SCAN (Android 12+)
- ACCESS_FINE_LOCATION (蓝牙扫描需要)
