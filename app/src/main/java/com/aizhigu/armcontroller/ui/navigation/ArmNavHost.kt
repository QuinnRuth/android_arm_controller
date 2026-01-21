package com.aizhigu.armcontroller.ui.navigation

import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aizhigu.armcontroller.ui.ArmViewModel
import com.aizhigu.armcontroller.ui.ActionSequencerViewModel
import com.aizhigu.armcontroller.ui.ConnectionScreen
import com.aizhigu.armcontroller.ui.DashboardScreen
import com.aizhigu.armcontroller.ui.SequencerScreen

@Composable
fun ArmNavHost(
    navController: NavHostController,
    viewModel: ArmViewModel,
    sequencerViewModel: ActionSequencerViewModel,
    bluetoothAdapter: BluetoothAdapter?,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Connection.route) {
            ConnectionScreen(
                viewModel = viewModel,
                bluetoothAdapter = bluetoothAdapter,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Connection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                sequencerViewModel = sequencerViewModel,
                onNavigateToConnection = {
                    navController.navigate(Screen.Connection.route)
                }
            )
        }
        
        // Sequencer is currently embedded in Dashboard tabs, 
        // but can be separated if needed later.
        composable(Screen.Sequencer.route) {
            SequencerScreen(
                viewModel = sequencerViewModel,
                currentServoValues = viewModel.servoValues.toList()
            )
        }
        
        composable(Screen.Settings.route) {
            // TODO: Settings Screen
        }
    }
}
