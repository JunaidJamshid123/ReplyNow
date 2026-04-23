package com.example.replynow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    data object Splash : Screen("splash", "Splash", null)
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object AppMessages : Screen("app_messages/{packageName}/{appName}/{iconRes}/{accentColor}", "Messages", null) {
        fun createRoute(packageName: String, appName: String, iconRes: Int, accentColor: Long): String =
            "app_messages/$packageName/$appName/$iconRes/$accentColor"
    }
}
