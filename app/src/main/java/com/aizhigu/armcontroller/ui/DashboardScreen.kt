package com.aizhigu.armcontroller.ui

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ArmViewModel,
    sequencerViewModel: ActionSequencerViewModel,
    onNavigateToConnection: () -> Unit
) {
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
                        } else {
                            Text(
                                "未连接",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToConnection) {
                        Icon(
                            imageVector = if (viewModel.isConnected) Icons.Filled.BluetoothConnected else Icons.Filled.BluetoothDisabled,
                            contentDescription = "连接设置",
                            tint = if (viewModel.isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
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
                SequencerScreen(
                    viewModel = sequencerViewModel,
                    currentServoValues = viewModel.servoValues.toList()
                )
            }
        }
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
        // 控制按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.centerAll() },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("全部中位", fontSize = 14.sp)
            }
            
            OutlinedButton(
                onClick = { viewModel.armRobot() },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ARM 启动", fontSize = 14.sp)
            }
        }
        
        val servoNames = listOf("J1 底座", "J2 大臂", "J3 小臂", "J4 腕俯仰", "J5 腕旋转", "J6 夹爪")
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
