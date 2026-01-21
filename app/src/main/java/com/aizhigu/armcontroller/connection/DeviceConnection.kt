package com.aizhigu.armcontroller.connection

import kotlinx.coroutines.flow.StateFlow

/**
 * 设备连接状态
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * 设备信息数据类
 */
data class DeviceInfo(
    val id: String,           // 唯一标识 (MAC地址 或 IP:Port)
    val name: String,         // 设备名称
    val type: ConnectionType, // 连接类型
    val rssi: Int? = null     // 信号强度 (仅BLE)
)

/**
 * 连接类型枚举
 */
enum class ConnectionType {
    BLE,
    WIFI
}

/**
 * 统一设备连接接口
 * BLE 和 WiFi 实现此接口
 */
interface DeviceConnection {
    /** 设备信息 */
    val deviceInfo: DeviceInfo
    
    /** 连接状态 Flow */
    val connectionState: StateFlow<ConnectionState>
    
    /** 建立连接 */
    suspend fun connect(): Boolean
    
    /** 断开连接 */
    suspend fun disconnect()
    
    /** 发送命令 (原始字符串格式，如 #1P1500T100!) */
    suspend fun sendCommand(command: String): Boolean
    
    /** 发送 JSON 格式命令 */
    suspend fun sendJson(json: String): Boolean
    
    /** 接收数据回调 */
    fun setOnDataReceived(callback: (ByteArray) -> Unit)
}
