package com.example.replynow.ui.screen

import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.replynow.service.ReplyNotificationListenerService
import com.example.replynow.ui.viewmodel.HomeViewModel
import com.example.replynow.ui.viewmodel.InstalledApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAppClick: (String, String, Int, Long) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Responsive: adaptive horizontal padding + grid min cell size
    val horizontalPadding = when {
        screenWidthDp >= 840 -> 32.dp  // tablet
        screenWidthDp >= 600 -> 24.dp  // large phone / small tablet
        else -> 16.dp                  // phone
    }
    val gridMinCellSize = when {
        screenWidthDp >= 840 -> 130.dp
        screenWidthDp >= 600 -> 115.dp
        screenWidthDp >= 380 -> 100.dp
        else -> 88.dp                  // very small phone
    }
    val titleSize = if (screenWidthDp >= 600) 30.sp else 26.sp
    val subtitleSize = if (screenWidthDp >= 600) 15.sp else 13.sp

    // Check notification access — re-check when resuming
    var hasNotificationAccess by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationAccess = isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "ReplyNow",
                            fontWeight = FontWeight.Bold,
                            fontSize = titleSize,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (hasNotificationAccess) {
                            Text(
                                if (uiState.totalPending > 0)
                                    "${uiState.totalPending} pending replies across apps"
                                else "All caught up! No pending replies",
                                fontSize = subtitleSize,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = gridMinCellSize),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notification access banner
            if (!hasNotificationAccess) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NotificationAccessBanner(
                        onGrantClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            )
                        }
                    )
                }
            }

            // Section header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "${uiState.installedApps.size} installed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            if (uiState.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.installedApps.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📱", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No supported apps installed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                itemsIndexed(uiState.installedApps) { index, app ->
                    val entryAlpha by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(400, delayMillis = index * 50, easing = FastOutSlowInEasing),
                        label = "entryAlpha"
                    )

                    AppGridItem(
                        app = app,
                        modifier = Modifier
                            .graphicsLayer { alpha = entryAlpha }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onAppClick(app.packageName, app.name, app.iconRes, app.accentColor) }
                            )
                    )
                }
            }

            // Bottom spacer
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun NotificationAccessBanner(onGrantClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Enable Notification Access",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "ReplyNow needs permission to read notifications from your messaging apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Grant Access", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AppGridItem(
    app: InstalledApp,
    modifier: Modifier = Modifier
) {
    val color = Color(app.accentColor)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    // Responsive icon + badge sizing
    val iconSize: Dp = when {
        screenWidthDp >= 840 -> 68.dp
        screenWidthDp >= 600 -> 62.dp
        screenWidthDp >= 380 -> 56.dp
        else -> 48.dp
    }
    val innerIconSize = iconSize * 0.71f
    val badgeSize: Dp = when {
        screenWidthDp >= 600 -> 24.dp
        else -> 22.dp
    }
    val nameFontSize = when {
        screenWidthDp >= 600 -> 14.sp
        else -> 12.sp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = if (isDark) 0.12f else 0.06f),
                        color.copy(alpha = if (isDark) 0.05f else 0.02f)
                    )
                )
            )
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .shadow(6.dp, CircleShape, ambientColor = color.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF2A2A2A) else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = app.iconRes),
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(innerIconSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }

            // Dynamic badge
            if (app.pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(badgeSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (app.pendingCount > 99) "99+" else "${app.pendingCount}",
                        color = Color.White,
                        fontSize = if (app.pendingCount > 99) 8.sp else 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            fontSize = nameFontSize
        )
    }
}

private fun isNotificationAccessGranted(context: android.content.Context): Boolean {
    val cn = ComponentName(context, ReplyNotificationListenerService::class.java)
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(cn.flattenToString())
}

