package com.aizhigu.armcontroller.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

/**
 * 经典蓝牙 SPP 连接实现
 * 用于 HC-05/HC-06 等经典蓝牙串口模块
 */
@SuppressLint("MissingPermission")
class ClassicBluetoothConnectionImpl(
    private val device: BluetoothDevice
) : DeviceConnection {
    
    companion object {
        // SPP UUID (经典蓝牙串口协议)
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
    
    override val deviceInfo: DeviceInfo = DeviceInfo(
        id = device.address,
        name = device.name ?: "Unknown Classic BT",
        type = ConnectionType.BLE // 复用 BLE 类型，实际是经典蓝牙
    )
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var onDataReceivedCallback: ((ByteArray) -> Unit)? = null
    
    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            
            bluetoothSocket?.close()
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            
            _connectionState.value = ConnectionState.CONNECTED
            true
        } catch (e: IOException) {
            e.printStackTrace()
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }
    
    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                outputStream?.close()
                bluetoothSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    override suspend fun sendCommand(command: String): Boolean = withContext(Dispatchers.IO) {
        try {
            outputStream?.write(command.toByteArray())
            outputStream?.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun sendJson(json: String): Boolean {
        return sendCommand(json)
    }
    
    override fun setOnDataReceived(callback: (ByteArray) -> Unit) {
        onDataReceivedCallback = callback
    }
}
