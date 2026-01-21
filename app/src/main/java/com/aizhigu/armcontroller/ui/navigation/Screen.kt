package com.aizhigu.armcontroller.ui.navigation

sealed class Screen(val route: String) {
    data object Connection : Screen("connection")
    data object Dashboard : Screen("dashboard")
    data object Sequencer : Screen("sequencer")
    data object Settings : Screen("settings")
}
