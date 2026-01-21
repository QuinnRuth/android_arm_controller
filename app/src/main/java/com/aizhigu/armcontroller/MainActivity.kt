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
import androidx.navigation.compose.rememberNavController
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.aizhigu.armcontroller.data.AppDatabase
import com.aizhigu.armcontroller.ui.ActionSequencerViewModel
import com.aizhigu.armcontroller.ui.ActionSequencerViewModelFactory
import com.aizhigu.armcontroller.ui.ArmViewModel
import com.aizhigu.armcontroller.ui.navigation.ArmNavHost
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
                    val armViewModel: ArmViewModel = viewModel()
                    
                    val database = AppDatabase.getDatabase(applicationContext)
                    val sequencerViewModel: ActionSequencerViewModel = viewModel(
                        factory = ActionSequencerViewModelFactory(
                            actionDao = database.actionDao(),
                            onSendCommand = { cmd -> armViewModel.sendRaw(cmd) }
                        )
                    )

                    val navController = androidx.navigation.compose.rememberNavController()
                    
                    ArmNavHost(
                        navController = navController,
                        viewModel = armViewModel,
                        sequencerViewModel = sequencerViewModel,
                        bluetoothAdapter = bluetoothAdapter
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

