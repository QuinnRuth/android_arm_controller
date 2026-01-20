# Android CI/CD 与嵌入式开发避坑指南

**创建时间**: 2026-01-20
**项目**: Android Robot Arm Controller
**背景**: 本项目旨在通过 GitHub Actions 自动构建 Android APK，并通过蓝牙控制 Arduino 机械臂。在此过程中经历了多次构建失败，特此总结。

---

## 🛠️ 第一部分：GitHub Actions CI/CD 踩坑记录

### 1. 致命错误：AndroidX 未启用
**现象**：
构建一开始就失败，提示项目使用了 AndroidX 依赖但未开启支持。

**错误日志**：
```text
This project uses AndroidX dependencies, but the 'android.useAndroidX' property is not enabled.
```

**✅ 解决方案**：
在项目根目录创建或修改 `gradle.properties`，必须包含以下内容：
```properties
# 必须开启 AndroidX 支持
android.useAndroidX=true
# 自动迁移第三方库
android.enableJetifier=true
# 避免配置缓存导致的奇怪错误
org.gradle.configuration-cache=false
# 压制旧 SDK 警告
android.suppressUnsupportedCompileSdk=34
```

---

### 2. 致命错误：Gradle 与 AGP 版本不兼容
**现象**：
GitHub Actions 默认环境（Ubuntu-latest）可能使用了最新的 Gradle（如 9.x），而你的 `build.gradle.kts` 中指定的 Android Gradle Plugin (AGP) 版本（如 8.2.2）不支持该 Gradle 版本。

**错误日志**：
```text
Cannot mutate the dependencies of configuration ':app:debugCompileClasspath' after the configuration was resolved.
```

**❌ 失败尝试**：
- 试图修改 BOM 依赖写法（无效）。
- 试图使用 `setup-gradle` 但未指定版本（默认用了最新版，导致失败）。

**✅ 解决方案 (关键)**：
在 `.github/workflows/android.yml` 中**强制指定**兼容的 Gradle 版本。对于 AGP 8.2.2，**Gradle 8.4** 是最佳拍档。

```yaml
- name: Setup Gradle
  uses: gradle/actions/setup-gradle@v4
  with:
    gradle-version: '8.4'  # 👈 必须锁死这个版本！不要用默认！
```

---

### 3. 编译错误：缺少 Material Icons
**现象**：
代码中使用了 `Icons.Filled.Bluetooth` 或 `Icons.Filled.BluetoothConnected`，但在 CI 构建时报错找不到引用。

**错误日志**：
```text
Unresolved reference: Bluetooth
Unresolved reference: BluetoothConnected
```

**原因**：
Compose 的默认 `material3` 包只包含最核心的图标。蓝牙图标属于扩展包。

**✅ 解决方案**：
在 `app/build.gradle.kts` 中添加扩展依赖：
```kotlin
implementation("androidx.compose.material:material-icons-extended:$composeUiVersion")
```

---

### 4. 编译错误：ViewModelScope 未解析
**现象**：
代码中使用了 `viewModelScope.launch`，但构建提示找不到 `viewModelScope`。

**错误日志**：
```text
Unresolved reference: viewModelScope
```

**原因**：
这是一个“双重陷阱”。
1. 缺少依赖：需要 `lifecycle-viewmodel-ktx`。
2. **缺少 Import**：即使加了依赖，有些情况下（特别是 CI 环境严苛检查时）必须显式 import 扩展属性。

**✅ 解决方案**：
第一步：`app/build.gradle.kts` 添加依赖
```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
```

第二步：**Kotlin 文件头部必须添加 Import** (最容易忘！)
```kotlin
import androidx.lifecycle.viewModelScope
```

---

## 📱 第二部分：蓝牙配对与连接

### 1. 无法识别设备名称
**现象**：
在手机蓝牙设置里扫描，只看到一堆 MAC 地址（如 `98:D3:31:F5:8A`），看不到 "HC-05" 或 "RobotArm"。

**✅ 解决方案（断电法）**：
1. **拔电**：拔掉机械臂电源。
2. **扫描**：手机刷新列表。
3. **上电**：插上电源。
4. **观察**：新出现的 MAC 地址就是目标设备。

### 2. 配对密码
默认通常是 `1234` 或 `0000`。

---

## 🤖 第三部分：Arduino 烧录Checklist

每次修改代码重新烧录前，务必检查：

1. **端口占用**：
   - 必须关闭所有串口监视器（VS Code 终端、Arduino IDE 监视器）。
   - 必须关闭正在运行的 Python/PowerShell 控制脚本。
   
2. **烧录命令 (PowerShell)**：
   ```powershell
   # 编译并烧录到 COM4
   arduino-cli compile -b arduino:avr:nano -p COM4 -u E:\web\embedded\arduino_robot_arm
   ```

3. **复位逻辑**：
   - 烧录完后，程序默认处于 `DISARM`（安全锁）状态。
   - 需要在 App 上点“ARM 启动”或发送 `ARM` 指令才能动。

---

*Verified by Antigravity CI Team, 2026-01-20*
