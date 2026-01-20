package com.aizhigu.armcontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aizhigu.armcontroller.ui.ArmControllerApp
import com.aizhigu.armcontroller.ui.theme.ArmControllerTheme
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : ComponentActivity() {
    
    private val bluetoothManager by lazy { getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter by lazy { bluetoothManager?.adapter }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Toast.makeText(this, "需要蓝牙权限才能使用", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestBluetoothPermissions()
        
        setContent {
            ArmControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ArmViewModel = viewModel()
                    ArmControllerApp(
                        viewModel = viewModel,
                        bluetoothAdapter = bluetoothAdapter,
                        onRequestPermissions = { requestBluetoothPermissions() }
                    )
                }
            }
        }
    }
    
    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        
        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (notGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}

// ViewModel 管理机械臂状态
class ArmViewModel : ViewModel() {
    // 6轴 PWM 值 (500-2500)
    var servoValues = mutableStateListOf(1500, 1500, 1500, 1500, 1500, 1500)
        private set
    
    var isConnected by mutableStateOf(false)
        private set
    
    var connectedDeviceName by mutableStateOf("")
        private set
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    
    companion object {
        // SPP UUID (经典蓝牙串口协议)
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
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
        // 发送急停命令
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
    
    private fun sendRaw(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                outputStream?.write(data.toByteArray())
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bluetoothSocket?.close()
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                
                withContext(Dispatchers.Main) {
                    isConnected = true
                    connectedDeviceName = device.name ?: "Unknown"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isConnected = false
                    connectedDeviceName = ""
                }
            }
        }
    }
    
    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                outputStream?.close()
                bluetoothSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                isConnected = false
                connectedDeviceName = ""
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
