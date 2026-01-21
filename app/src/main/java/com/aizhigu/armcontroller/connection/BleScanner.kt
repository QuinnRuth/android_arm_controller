package com.aizhigu.armcontroller.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * BLE 设备扫描器
 */
class BleScanner(private val context: Context) {
    
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    
    /**
     * 检查是否有蓝牙权限
     */
    fun hasPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查蓝牙是否启用
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    /**
     * 扫描 BLE 设备
     * 返回 Flow 流式发现设备
     */
    fun scan(): Flow<BluetoothDevice> = callbackFlow {
        if (!hasPermissions()) {
            close(SecurityException("Missing Bluetooth permissions"))
            return@callbackFlow
        }
        
        if (!isBluetoothEnabled()) {
            close(IllegalStateException("Bluetooth is disabled"))
            return@callbackFlow
        }
        
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            close(IllegalStateException("BLE Scanner not available"))
            return@callbackFlow
        }
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        val discoveredDevices = mutableSetOf<String>()
        
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (device.address !in discoveredDevices) {
                    discoveredDevices.add(device.address)
                    trySend(device)
                }
            }
            
            override fun onScanFailed(errorCode: Int) {
                close(Exception("BLE scan failed with error code: $errorCode"))
            }
        }
        
        try {
            scanner.startScan(emptyList(), settings, callback)
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }
        
        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (e: SecurityException) {
                // Ignore
            }
        }
    }
    
    /**
     * 通过 MAC 地址获取设备
     */
    fun getDeviceByAddress(address: String): BluetoothDevice? {
        return try {
            bluetoothAdapter?.getRemoteDevice(address)
        } catch (e: Exception) {
            null
        }
    }
}
