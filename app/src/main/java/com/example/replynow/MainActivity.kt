package com.example.replynow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.replynow.ui.navigation.ReplyNowNavHost
import com.example.replynow.ui.navigation.Screen
import com.example.replynow.ui.theme.ReplyNowTheme
import com.example.replynow.worker.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleReminderWork()
        setContent {
            ReplyNowTheme {
                ReplyNowMainScreen()
            }
        }
    }

    private fun scheduleReminderWork() {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyNowMainScreen() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isSplash = currentDestination?.route == Screen.Splash.route
    val isAppMessages = currentDestination?.route == Screen.AppMessages.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isSplash && !isAppMessages) {
                NavigationBar(
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        ReplyNowNavHost(
            navController = navController,
            modifier = if (isSplash || isAppMessages) Modifier else Modifier.padding(innerPadding)
        )
    }
}