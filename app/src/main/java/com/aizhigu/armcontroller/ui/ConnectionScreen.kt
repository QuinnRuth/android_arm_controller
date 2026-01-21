package com.aizhigu.armcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    viewModel: ArmViewModel,
    bluetoothAdapter: BluetoothAdapter?,
    onNavigateToDashboard: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Get paired devices
    val pairedDevices = remember(bluetoothAdapter) {
        bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("连接设备") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isConnected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (viewModel.isConnected) Icons.Default.CheckCircle else Icons.Default.Bluetooth,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (viewModel.isConnected) "已连接" else "未连接",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (viewModel.isConnected) {
                            Text(text = viewModel.connectedDeviceName)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (viewModel.isConnected) {
                        Button(onClick = { viewModel.disconnect() }) {
                            Text("断开")
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavigateToDashboard,
                    modifier = Modifier.weight(1f),
                    enabled = viewModel.isConnected
                ) {
                    Text("进入控制台")
                }
                
                OutlinedButton(
                    onClick = { /* TODO: Implement Discovery */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("扫描设备")
                }
            }

            Text(
                "已配对设备",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pairedDevices.isEmpty()) {
                    item {
                        Text(
                            "没有已配对的设备，请在系统蓝牙设置中配对。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                            items(pairedDevices) { device ->
                                DeviceListItem(
                                    device = device,
                                    isConnected = viewModel.isConnected && viewModel.connectedDeviceName == device.name,
                                    onClick = {
                                        viewModel.connectToDevice(device, context)
                                        // Optional: Auto-navigate if connection logic was callback-based. 
                                        // Since it's async in VM, user manually clicks "Enter Dashboard" or we observe VM state.
                                    }
                                )
                            }
                }
            }
        }
    }
    
    // Auto-navigate effect
    LaunchedEffect(viewModel.isConnected) {
        if (viewModel.isConnected) {
            onNavigateToDashboard()
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceListItem(
    device: BluetoothDevice,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            headlineContent = { Text(device.name ?: "Unknown Device") },
            supportingContent = { Text(device.address) },
            leadingContent = {
                Icon(Icons.Default.Bluetooth, contentDescription = null)
            },
            trailingContent = {
                if (isConnected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}
