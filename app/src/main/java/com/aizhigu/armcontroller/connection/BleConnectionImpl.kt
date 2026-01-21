package com.aizhigu.armcontroller.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.suspend
import java.util.UUID
import kotlin.coroutines.resume

/**
 * BLE 连接实现
 * 使用 Nordic Semiconductor BLE Library
 */
class BleConnectionImpl(
    private val context: Context,
    private val device: BluetoothDevice
) : DeviceConnection {
    
    companion object {
        // 自定义 GATT Service UUID (需与 ESP32 固件一致)
        val SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        // RX Characteristic - 写入数据到设备
        val RX_CHAR_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        // TX Characteristic - 从设备接收数据
        val TX_CHAR_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    }
    
    override val deviceInfo: DeviceInfo = DeviceInfo(
        id = device.address,
        name = device.name ?: "Unknown BLE Device",
        type = ConnectionType.BLE
    )
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var onDataReceivedCallback: ((ByteArray) -> Unit)? = null
    
    private val bleManager = object : BleManager(context) {
        private var rxCharacteristic: BluetoothGattCharacteristic? = null
        private var txCharacteristic: BluetoothGattCharacteristic? = null
        
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(SERVICE_UUID)
            if (service != null) {
                rxCharacteristic = service.getCharacteristic(RX_CHAR_UUID)
                txCharacteristic = service.getCharacteristic(TX_CHAR_UUID)
            }
            return rxCharacteristic != null && txCharacteristic != null
        }

        // Public wrapper for protected writeCharacteristic
        // Public wrapper returning WriteRequest
        fun writeRx(data: ByteArray) = 
            writeCharacteristic(
                rxCharacteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        
        override fun initialize() {
            // 设置通知回调
            setNotificationCallback(txCharacteristic).with { device, data ->
                data.value?.let { bytes ->
                    onDataReceivedCallback?.invoke(bytes)
                }
            }
            enableNotifications(txCharacteristic).enqueue()
        }
        
        override fun onServicesInvalidated() {
            rxCharacteristic = null
            txCharacteristic = null
        }
        
    }
    
    override suspend fun connect(): Boolean {
        return try {
            _connectionState.value = ConnectionState.CONNECTING
            
            bleManager.connect(device)
                .retry(3, 500)
                .useAutoConnect(false)
                .timeout(10000)
                .suspend()
            
            _connectionState.value = ConnectionState.CONNECTED
            true
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }
    
    override suspend fun disconnect() {
        try {
            bleManager.disconnect().suspend()
        } catch (e: Exception) {
            // Ignore disconnect errors
        }
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    override suspend fun sendCommand(command: String): Boolean {
        return sendBytes(command.toByteArray(Charsets.UTF_8))
    }
    
    override suspend fun sendJson(json: String): Boolean {
        return sendCommand(json)
    }
    
    private suspend fun sendBytes(data: ByteArray): Boolean {
        return try {
            bleManager.writeRx(data).suspend()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun setOnDataReceived(callback: (ByteArray) -> Unit) {
        onDataReceivedCallback = callback
    }
}
