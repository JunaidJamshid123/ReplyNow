package com.example.replynow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    data object Splash : Screen("splash", "Splash", null)
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
