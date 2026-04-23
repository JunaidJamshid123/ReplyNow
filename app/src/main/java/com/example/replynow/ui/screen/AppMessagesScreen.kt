package com.example.replynow.ui.screen

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.replynow.service.ReplyNotificationListenerService
import com.example.replynow.ui.viewmodel.AppMessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMessagesScreen(
    appName: String,
    iconRes: Int,
    accentColor: Long,
    onBack: () -> Unit,
    viewModel: AppMessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val color = Color(accentColor)
    val context = LocalContext.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Responsive sizing
    val horizontalPadding = when {
        screenWidthDp >= 840 -> 48.dp
        screenWidthDp >= 600 -> 32.dp
        else -> 16.dp
    }
    val maxCardWidth = if (screenWidthDp >= 840) 640.dp else Dp.Unspecified
    val topBarIconSize: Dp = if (screenWidthDp >= 600) 44.dp else 38.dp
    val topBarInnerIcon: Dp = if (screenWidthDp >= 600) 32.dp else 28.dp
    val titleFontSize = if (screenWidthDp >= 600) 22.sp else 20.sp

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(topBarIconSize)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2A2A2A) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = appName,
                                modifier = Modifier.size(topBarInnerIcon).clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                appName,
                                fontWeight = FontWeight.Bold,
                                fontSize = titleFontSize,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${uiState.messages.size} pending ${if (uiState.messages.size == 1) "chat" else "chats"}",
                                fontSize = 12.sp,
                                color = color
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = color
                )
            } else if (uiState.messages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = if (isDark) 0.15f else 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = color
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "All caught up!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "No pending messages on $appName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(uiState.messages, key = { _, msg -> msg.id }) { index, message ->
                        val entryAlpha by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(350, delayMillis = index * 60, easing = FastOutSlowInEasing),
                            label = "msgAlpha"
                        )
                        PendingMessageCard(
                            senderName = message.senderName,
                            messagePreview = message.messagePreview,
                            timestamp = message.timestamp,
                            isImportant = message.isImportant,
                            messageCount = message.messageCount,
                            accentColor = color,
                            isDark = isDark,
                            packageName = message.packageName,
                            screenWidthDp = screenWidthDp,
                            maxCardWidth = maxCardWidth,
                            onMarkReplied = { viewModel.markAsReplied(message.id) },
                            onSnooze = { duration -> viewModel.snooze(message.id, duration) },
                            onOpenChat = {
                                // Try cached PendingIntent first → then fallback to app launch
                                val key = "${message.packageName}_${message.senderName}"
                                val pi = ReplyNotificationListenerService.pendingIntents[key]
                                if (pi != null) {
                                    try { pi.send() } catch (_: Exception) { launchApp(context, message.packageName) }
                                } else {
                                    launchApp(context, message.packageName)
                                }
                            },
                            modifier = Modifier.graphicsLayer { alpha = entryAlpha }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

private fun launchApp(context: android.content.Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Composable
private fun PendingMessageCard(
    senderName: String,
    messagePreview: String,
    timestamp: Long,
    isImportant: Boolean,
    messageCount: Int,
    accentColor: Color,
    isDark: Boolean,
    packageName: String,
    screenWidthDp: Int,
    maxCardWidth: Dp,
    onMarkReplied: () -> Unit,
    onSnooze: (Long) -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSnooze by remember { mutableStateOf(false) }
    val cardBg = if (isDark) {
        if (isImportant) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        if (isImportant) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surface
    }

    // Responsive sizing
    val avatarSize: Dp = when {
        screenWidthDp >= 600 -> 56.dp
        screenWidthDp >= 380 -> 48.dp
        else -> 42.dp
    }
    val avatarFontSize = when {
        screenWidthDp >= 600 -> 24.sp
        screenWidthDp >= 380 -> 20.sp
        else -> 17.sp
    }
    val cardPadding: Dp = when {
        screenWidthDp >= 600 -> 20.dp
        else -> 16.dp
    }
    val buttonFontSize = when {
        screenWidthDp >= 380 -> 12.sp
        else -> 11.sp
    }
    val isCompact = screenWidthDp < 340

    val cardModifier = if (maxCardWidth != Dp.Unspecified) {
        modifier.widthIn(max = maxCardWidth).fillMaxWidth()
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier
            .animateContentSize(tween(300))
            .clickable { onOpenChat() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            // Top row: avatar + name + count badge + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sender avatar with app accent gradient
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.8f),
                                    accentColor
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        senderName.first().uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = avatarFontSize
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            senderName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isImportant) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Important",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        if (messageCount > 1) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = if (isDark) 0.3f else 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "$messageCount msgs",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentColor
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        formatTimeAgo(timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Message preview with left accent bar
            Row {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor.copy(alpha = 0.5f))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    messagePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons — stack vertically on very compact screens
            if (isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenChat,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Open Chat", fontSize = buttonFontSize, fontWeight = FontWeight.SemiBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onMarkReplied,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Replied", fontSize = buttonFontSize, fontWeight = FontWeight.SemiBold)
                        }
                        FilledTonalButton(
                            onClick = { showSnooze = !showSnooze },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenChat,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Open", fontSize = buttonFontSize, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onMarkReplied,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Replied", fontSize = buttonFontSize, fontWeight = FontWeight.SemiBold)
                    }
                    FilledTonalButton(
                        onClick = { showSnooze = !showSnooze },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                    }
                }
            }

            // Snooze options
            AnimatedVisibility(
                visible = showSnooze,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "15 min" to 15 * 60 * 1000L,
                        "1 hour" to 60 * 60 * 1000L,
                        "Tonight" to getTonightMillis()
                    ).forEach { (label, duration) ->
                        AssistChip(
                            onClick = { onSnooze(duration); showSnooze = false },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}

private fun getTonightMillis(): Long {
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 21)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
    }
    return cal.timeInMillis - System.currentTimeMillis()
}
