package com.aizhigu.armcontroller.connection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 多设备连接管理器
 * 统一管理 BLE 和 WiFi 连接
 */
class ConnectionManager {
    
    private val _connections = MutableStateFlow<Map<String, DeviceConnection>>(emptyMap())
    val connections: StateFlow<Map<String, DeviceConnection>> = _connections.asStateFlow()
    
    private val _activeDeviceId = MutableStateFlow<String?>(null)
    val activeDeviceId: StateFlow<String?> = _activeDeviceId.asStateFlow()
    
    /**
     * 获取当前活跃设备
     */
    fun getActiveDevice(): DeviceConnection? {
        val id = _activeDeviceId.value ?: return null
        return _connections.value[id]
    }
    
    /**
     * 设置活跃设备
     */
    fun setActiveDevice(deviceId: String) {
        if (_connections.value.containsKey(deviceId)) {
            _activeDeviceId.value = deviceId
        }
    }
    
    /**
     * 添加连接
     */
    suspend fun addConnection(connection: DeviceConnection): Boolean {
        val success = connection.connect()
        if (success) {
            _connections.value = _connections.value + (connection.deviceInfo.id to connection)
            // 如果是第一个设备，自动设为活跃
            if (_activeDeviceId.value == null) {
                _activeDeviceId.value = connection.deviceInfo.id
            }
        }
        return success
    }
    
    /**
     * 移除连接
     */
    suspend fun removeConnection(deviceId: String) {
        _connections.value[deviceId]?.disconnect()
        _connections.value = _connections.value - deviceId
        
        // 如果移除的是活跃设备，切换到其他设备
        if (_activeDeviceId.value == deviceId) {
            _activeDeviceId.value = _connections.value.keys.firstOrNull()
        }
    }
    
    /**
     * 向活跃设备发送命令
     */
    suspend fun sendToActive(command: String): Boolean {
        return getActiveDevice()?.sendCommand(command) ?: false
    }
    
    /**
     * 向所有已连接设备广播命令
     */
    suspend fun broadcast(command: String): Int {
        var successCount = 0
        _connections.value.values.forEach { conn ->
            if (conn.connectionState.value == ConnectionState.CONNECTED) {
                if (conn.sendCommand(command)) {
                    successCount++
                }
            }
        }
        return successCount
    }
    
    /**
     * 向指定类型的设备发送命令
     */
    suspend fun sendByType(command: String, type: ConnectionType): Int {
        var successCount = 0
        _connections.value.values
            .filter { it.deviceInfo.type == type }
            .forEach { conn ->
                if (conn.connectionState.value == ConnectionState.CONNECTED) {
                    if (conn.sendCommand(command)) {
                        successCount++
                    }
                }
            }
        return successCount
    }
    
    /**
     * 断开所有连接
     */
    suspend fun disconnectAll() {
        _connections.value.values.forEach { it.disconnect() }
        _connections.value = emptyMap()
        _activeDeviceId.value = null
    }
    
    /**
     * 获取已连接设备数量
     */
    fun getConnectedCount(): Int {
        return _connections.value.values.count { 
            it.connectionState.value == ConnectionState.CONNECTED 
        }
    }
    
    /**
     * 按类型获取设备列表
     */
    fun getDevicesByType(type: ConnectionType): List<DeviceConnection> {
        return _connections.value.values.filter { it.deviceInfo.type == type }
    }
}
