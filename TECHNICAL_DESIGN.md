# 📱 Android 机械臂上位机技术架构文档 (Technical Design)

本文档详细说明了 Android 蓝牙机械臂控制器的技术栈、通信原本、以及动作组 (Action Sequencer) 的实现流程。

---

## 🏗️ 1. 技术栈 (Tech Stack)

本项目采用现代 Android 开发标准（2024+）构建：

- **编程语言**: [Kotlin](https://kotlinlang.org/) (100% 纯 Kotlin)
- **UI 框架**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (使用 Material3 Design)
- **架构模式**: **MVVM** (Model-View-ViewModel)
  - `ViewModel`: 管理 UI 状态，生命周期感知
- **数据持久化**: [Room Database](https://developer.android.com/training/data-storage/room)
  - 本地 SQLite 数据库封装，用于保存动作工程 (`ActionProject`) 和动作帧 (`ActionFrame`)
- **通信协议**: **Bluetooth Classic (SPP)**
  - 使用标准的 RFCOMM 通道通信

---

## 📡 2. 蓝牙通信原理 (Bluetooth Communication)

上位机与 Arduino 下位机通过蓝牙 **SPP (Serial Port Profile)** 协议进行通信。

### 2.1 连接建立
1. **UUID**: 使用标准 SPP UUID `00001101-0000-1000-8000-00805F9B34FB`。
2. **Socket**: `BluetoothDevice.createRfcommSocketToServiceRecord(uuid)` 创建套接字。
3. **IO 流**: 获取 `OutputStream` 用于发送指令。

```kotlin
// 核心连接代码 (ArmViewModel.kt)
bluetoothAdapter?.getRemoteDevice(address)?.let { device ->
    socket = device.createRfcommSocketToServiceRecord(MY_UUID)
    socket?.connect()
    outputStream = socket?.outputStream
}
```

### 2.2 通信协议 (Protocol)
物理层通过串口传输 ASCII 字符串。协议遵循传统的舵机控制板格式：

**格式**: `#<ID>P<PWM>T<TIME>!`

- `#`: 指令起始符
- `<ID>`: 舵机 ID (1-6)
- `P`: 位置标识 (Pulse)
- `<PWM>`: 脉宽值，范围 500-2500 (对应 0°-180°)
- `T`: 时间标识 (Time)
- `<TIME>`: 运动时间，单位 ms
- `!`: 指令结束符 (本项目自定义或通用)

**示例**:
- `#1P1500T1000!`: 1号舵机移动到中位 (1500)，耗时 1000ms。
- `#6P2500T500!`: 6号舵机（夹爪）移动到最大位，耗时 500ms。

### 2.3 数据发送层
APP 通过 `ArmViewModel` 暴露的 `sendRaw` 方法将字符串转换为字节并写入流。

```kotlin
fun sendRaw(cmd: String) {
    viewModelScope.launch(Dispatchers.IO) {
        outputStream?.write(cmd.toByteArray()) // 将字符串转为 ASCII 字节发送
    }
}
```

---

## 🎬 3. Action Sequencer (动作编程) 实现流程

动作编程模块 (`ActionSequencerViewModel`) 是上位机的核心逻辑，它将静态的动作帧列表转换为连续的蓝牙指令。

### 3.1 架构设计
- **Model**: `ActionFrame` (包含 6 个舵机的 PWM 值和 1 个 Duration)。
- **ViewModel**: `ActionSequencerViewModel`。
  - 维护 `_frames` (当前编辑的帧列表)。
  - 维护 `_isPlaying` (播放状态)。
  - **关键依赖**:构造时传入 `onSendCommand: (String) -> Unit` 回调，桥接到底层的蓝牙发送函数。

### 3.2 运行流程 (The Execution Loop)

当用户点击 "RUN" 或 "LOOP" 时，启动协程执行以下循环：

1. **遍历帧 (Frame Iteration)**: 协程遍历 `List<ActionFrame>`。
2. **命令生成 (Command Gen)**:
   对于每一帧，ViewModel 会拆解为 6 条独立指令（或一条组合指令）：
   ```kotlin
   frame.servos.forEachIndexed { index, pwm ->
       val cmd = "#${index + 1}P${pwm}T${frame.duration}!"
       onSendCommand(cmd) // 通过回调发送给 ArmViewModel -> Bluetooth
   }
   ```
3. **时序控制 (Timing)**:
   发送完指令后，协程执行 `delay(frame.duration)`，挂起等待机械臂运动完成，再执行下一帧。
   ```kotlin
   executeFrame(frame)
   delay(frame.duration.toLong()) // 协程挂起，不阻塞 UI 线程
   ```
4. **位姿捕获 (Pose Capture)**:
   - "Add Keyframe" 功能直接读取 `ArmViewModel` 中当前的 `servoValues` (StateFlow)。
   - 因为 `servoValues` 实时反映了手动控制界面的滑块值，所以捕获的即是当前下发给机械臂的真实姿态。

### 3.3 .tox 文件解析
为了兼容 PC 上位机，通过 `ToxParser` 对象解析 XML：
- 使用 Regex 提取 `<Table1>` 块。
- 解析 `#nSV...Pn...` 格式提取舵机 PWM 值。
- 解析 `T...` 提取时间。
- 最终转换为 APP 内部的 `ActionFrame` 对象列表。

---

## 📂 4. 项目结构概览

```
com.aizhigu.armcontroller
├── data/
│   ├── ActionDao.kt          # Room 数据库访问接口
│   ├── ActionEntities.kt     # 数据表实体 (ActionProject, ActionFrame)
│   ├── AppDatabase.kt        # 数据库实例
│   └── ToxParser.kt          # .tox 文件解析器
├── ui/
│   ├── ActionSequencerViewModel.kt # 动作编程业务逻辑
│   ├── ArmControllerApp.kt         # 主 UI 框架 (Navigation)
│   ├── ArmViewModel.kt             # 蓝牙连接与手动控制逻辑
│   ├── ManualControlScreen.kt      # 手动控制界面
│   ├── SequencerScreen.kt          # 动作编程界面
│   └── theme/                      # Cyberpunk 主题定义
└── MainActivity.kt           # APP 入口与权限申请
```
