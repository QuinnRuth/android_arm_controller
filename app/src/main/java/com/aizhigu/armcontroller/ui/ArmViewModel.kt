package com.aizhigu.armcontroller.ui

import android.content.Context
import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aizhigu.armcontroller.connection.ClassicBluetoothConnectionImpl
import com.aizhigu.armcontroller.connection.WifiConnectionImpl
import com.aizhigu.armcontroller.connection.ConnectionManager
import com.aizhigu.armcontroller.connection.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArmViewModel : ViewModel() {
    private val connectionManager = ConnectionManager()
    
    // 6轴 PWM 值 (500-2500)
    var servoValues = mutableStateListOf(1500, 1500, 1500, 1500, 1500, 1500)
        private set
    
    var isConnected by mutableStateOf(false)
        private set

    var connectedDeviceName by mutableStateOf("")
        private set

    init {
        // Monitor active device state for UI updates
        viewModelScope.launch {
            connectionManager.activeDeviceId.collectLatest { activeId ->
                if (activeId == null) {
                    isConnected = false
                    connectedDeviceName = ""
                } else {
                    val device = connectionManager.getActiveDevice()
                    if (device != null) {
                        connectedDeviceName = device.deviceInfo.name
                        device.connectionState.collectLatest { state ->
                            isConnected = state == ConnectionState.CONNECTED
                        }
                    }
                }
            }
        }
    }

    fun updateServo(index: Int, value: Int) {
        val clampedValue = value.coerceIn(500, 2500)
        servoValues[index] = clampedValue
        sendCommand(index + 1, clampedValue)
    }
    
    fun centerAll() {
        for (i in 0..5) {
            servoValues[i] = 1500
        }
        // 发送所有轴回中位
        for (i in 1..6) {
            sendCommand(i, 1500)
        }
    }
    
    fun emergencyStop() {
        sendRaw("DISARM\n")
    }
    
    fun armRobot() {
        sendRaw("ARM\n")
    }
    
    private fun sendCommand(servo: Int, pwm: Int) {
        // 协议格式: #1P1500T100!
        val cmd = "#${servo}P${pwm}T50!"
        sendRaw(cmd)
    }
    
    public fun sendRaw(data: String) {
        viewModelScope.launch {
            connectionManager.sendToActive(data)
        }
    }
    
    fun connectToDevice(device: BluetoothDevice, context: Context) {
        viewModelScope.launch {
            val connection = ClassicBluetoothConnectionImpl(device)
            connectionManager.addConnection(connection)
        }
    }

    fun connectToWifi(host: String, port: Int = 81) {
        viewModelScope.launch {
            val connection = WifiConnectionImpl(host, port)
            connectionManager.addConnection(connection)
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            connectionManager.disconnectAll()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
