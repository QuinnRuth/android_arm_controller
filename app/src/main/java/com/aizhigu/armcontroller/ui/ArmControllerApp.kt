package com.aizhigu.armcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aizhigu.armcontroller.ArmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmControllerApp(
    viewModel: ArmViewModel,
    sequencerViewModel: ActionSequencerViewModel,
    bluetoothAdapter: BluetoothAdapter?,
    onRequestPermissions: () -> Unit
) {
    var showDeviceDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Manual, 1=Sequencer
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("六轴机械臂控制器", fontWeight = FontWeight.Bold)
                        if (viewModel.isConnected) {
                            Text(
                                "已连接: ${viewModel.connectedDeviceName}",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { 
                        if (viewModel.isConnected) {
                            viewModel.disconnect()
                        } else {
                            showDeviceDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = if (viewModel.isConnected) Icons.Filled.BluetoothConnected else Icons.Filled.Bluetooth,
                            contentDescription = "蓝牙",
                            tint = if (viewModel.isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null) },
                    label = { Text("手动控制") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("动作编程") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (selectedTab == 0) {
                ManualControlScreen(viewModel)
            } else {
                SequencerScreen(sequencerViewModel)
            }
        }
    }
    
    if (showDeviceDialog) {
        BluetoothDeviceDialog(
            bluetoothAdapter = bluetoothAdapter,
            onDeviceSelected = { device ->
                viewModel.connectToDevice(device)
                showDeviceDialog = false
            },
            onDismiss = { showDeviceDialog = false }
        )
    }
}

@Composable
fun ManualControlScreen(viewModel: ArmViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 急停按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.emergencyStop() },
                modifier = Modifier.weight(1f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("急停 STOP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = { viewModel.centerAll() },
                modifier = Modifier.weight(1f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("复位", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        OutlinedButton(
            onClick = { viewModel.armRobot() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ARM 启动")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        val servoNames = listOf("底座旋转", "大臂", "小臂", "手腕俯仰", "手腕旋转", "夹爪")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(6) { index ->
                ServoSlider(
                    name = servoNames[index],
                    servoIndex = index + 1,
                    value = viewModel.servoValues[index],
                    onValueChange = { viewModel.updateServo(index, it) }
                )
            }
        }
    }
}

@Composable
fun ServoSlider(
    name: String,
    servoIndex: Int,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "轴$servoIndex: $name",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$value μs",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 500f..2500f,
                steps = 0,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 快捷按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { onValueChange(500) }) { Text("最小") }
                TextButton(onClick = { onValueChange(1000) }) { Text("1000") }
                TextButton(onClick = { onValueChange(1500) }) { Text("中位") }
                TextButton(onClick = { onValueChange(2000) }) { Text("2000") }
                TextButton(onClick = { onValueChange(2500) }) { Text("最大") }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceDialog(
    bluetoothAdapter: BluetoothAdapter?,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onDismiss: () -> Unit
) {
    val pairedDevices = remember {
        bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择蓝牙设备") },
        text = {
            if (pairedDevices.isEmpty()) {
                Text("没有已配对的设备，请先在系统设置中配对 HC-05/HC-06")
            } else {
                LazyColumn {
                    items(pairedDevices) { device ->
                        ListItem(
                            headlineContent = { Text(device.name ?: "未知设备") },
                            supportingContent = { Text(device.address) },
                            leadingContent = {
                                Icon(Icons.Filled.Bluetooth, contentDescription = null)
                            },
                            modifier = Modifier.clickable { onDeviceSelected(device) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
