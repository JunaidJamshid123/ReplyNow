package com.example.replynow.ui.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.replynow.service.ReplyNotificationListenerService
import com.example.replynow.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Re-check statuses on resume
    var isBatteryOptimized by remember { mutableStateOf(true) }
    var hasNotificationAccess by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryOptimized = !isIgnoringBatteryOptimizations(context)
                hasNotificationAccess = isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val horizontalPadding = when {
        screenWidthDp >= 840 -> 48.dp
        screenWidthDp >= 600 -> 32.dp
        else -> 16.dp
    }
    val maxContentWidth = if (screenWidthDp >= 840) 640.dp else Int.MAX_VALUE.dp
    val titleSize = if (screenWidthDp >= 600) 28.sp else 24.sp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold, fontSize = titleSize)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = horizontalPadding)
                .widthIn(max = maxContentWidth)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notification Access
            SettingsCard(
                title = "Notification Access",
                subtitle = if (hasNotificationAccess) "Access granted — monitoring active" else "Required to detect incoming messages",
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
            ) {
                if (hasNotificationAccess) {
                    Text("Granted", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                } else {
                    Button(
                        onClick = { openNotificationListenerSettings(context) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Access")
                    }
                }
            }

            // Battery Optimization
            if (isBatteryOptimized) {
                SettingsCard(
                    title = "Battery Optimization",
                    subtitle = "Disable to prevent Android from killing the service",
                    icon = { Icon(Icons.Default.BatteryAlert, contentDescription = null) }
                ) {
                    Button(
                        onClick = { requestBatteryOptimizationExemption(context) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Disable")
                    }
                }
            }

            // Notifications toggle
            SettingsCard(
                title = "Reminders",
                subtitle = "Get notified about unreplied messages",
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
            ) {
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }

            // Reminder delay
            SettingsCard(
                title = "Reminder Delay",
                subtitle = "How long to wait before reminding you",
                icon = { Icon(Icons.Default.Timer, contentDescription = null) }
            ) {
                var expanded by remember { mutableStateOf(false) }
                val delayOptions = listOf(
                    "30 minutes" to 30 * 60_000L,
                    "1 hour" to 60 * 60_000L,
                    "2 hours" to 2 * 60 * 60_000L,
                    "4 hours" to 4 * 60 * 60_000L
                )
                val currentLabel = delayOptions.find { it.second == uiState.reminderDelayMillis }?.first ?: "1 hour"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .widthIn(min = 130.dp, max = 200.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        delayOptions.forEach { (label, millis) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.setReminderDelay(millis)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // About section
            Text(
                "ReplyNow v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    action: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.width(8.dp))
            action()
        }
    }
}

private fun openNotificationListenerSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    context.startActivity(intent)
}

private fun requestBatteryOptimizationExemption(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

fun isNotificationListenerEnabled(context: Context): Boolean {
    val cn = ComponentName(context, ReplyNotificationListenerService::class.java)
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(cn.flattenToString()) == true
}
