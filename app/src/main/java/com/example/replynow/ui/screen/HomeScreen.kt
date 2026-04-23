package com.example.replynow.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.replynow.R

data class AppItem(
    val name: String,
    val packageName: String,
    val iconRes: Int,
    val pendingCount: Int = 0,
    val accentColor: Color
)

private val supportedApps = listOf(
    AppItem("WhatsApp", "com.whatsapp", R.drawable.whatsapp, 3, Color(0xFF25D366)),
    AppItem("Messenger", "com.facebook.orca", R.drawable.messenger, 1, Color(0xFF0084FF)),
    AppItem("Instagram", "com.instagram.android", R.drawable.instagram, 2, Color(0xFFE1306C)),
    AppItem("Telegram", "org.telegram.messenger", R.drawable.telegram, 0, Color(0xFF0088CC)),
    AppItem("Snapchat", "com.snapchat.android", R.drawable.snapchat, 0, Color(0xFFFFFC00)),
    AppItem("Discord", "com.discord", R.drawable.discord, 1, Color(0xFF5865F2)),
    AppItem("Slack", "com.Slack", R.drawable.slack, 0, Color(0xFF4A154B)),
    AppItem("Teams", "com.microsoft.teams", R.drawable.teams, 0, Color(0xFF6264A7)),
    AppItem("Signal", "org.thoughtcrime.securesms", R.drawable.signal, 0, Color(0xFF3A76F0)),
    AppItem("Gmail", "com.google.android.gm", R.drawable.gmail, 4, Color(0xFFEA4335)),
    AppItem("Outlook", "com.microsoft.office.outlook", R.drawable.outlook, 0, Color(0xFF0078D4)),
    AppItem("LinkedIn", "com.linkedin.android", R.drawable.linkedin, 0, Color(0xFF0A66C2)),
    AppItem("Facebook", "com.facebook.katana", R.drawable.facebook, 0, Color(0xFF1877F2)),
    AppItem("X", "com.twitter.android", R.drawable.x, 0, Color(0xFF000000)),
    AppItem("TikTok", "com.zhiliaoapp.musically", R.drawable.tiktok, 0, Color(0xFF010101)),
    AppItem("Viber", "com.viber.voip", R.drawable.viber, 0, Color(0xFF7360F2)),
    AppItem("WeChat", "com.tencent.mm", R.drawable.wechat, 0, Color(0xFF07C160)),
    AppItem("Messages", "com.google.android.apps.messaging", R.drawable.messages, 1, Color(0xFF1A73E8)),
    AppItem("Google Chat", "com.google.android.apps.dynamite", R.drawable.gchat, 0, Color(0xFF00AC47)),
    AppItem("Zoom", "us.zoom.videomeetings", R.drawable.zoom, 0, Color(0xFF2D8CFF)),
    AppItem("WA Business", "com.whatsapp.w4b", R.drawable.whatsappbusiness, 0, Color(0xFF25D366)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val totalPending = supportedApps.sumOf { it.pendingCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "ReplyNow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "$totalPending pending replies across apps",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header — "Your Apps"
            item(span = { GridItemSpan(3) }) {
                Text(
                    "Connected Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            itemsIndexed(supportedApps) { index, app ->
                var pressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.92f else 1f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing),
                    label = "scale"
                )

                // Staggered entry animation
                val entryAlpha by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 400,
                        delayMillis = index * 50,
                        easing = FastOutSlowInEasing
                    ),
                    label = "entryAlpha"
                )

                AppGridItem(
                    app = app,
                    modifier = Modifier
                        .scale(scale)
                        .graphicsLayer { this.alpha = entryAlpha }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { pressed = !pressed }
                        )
                )
            }

            // Bottom spacer
            item(span = { GridItemSpan(3) }) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AppGridItem(
    app: AppItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        app.accentColor.copy(alpha = 0.06f),
                        app.accentColor.copy(alpha = 0.02f)
                    )
                )
            )
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Icon with subtle shadow
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(6.dp, CircleShape, ambientColor = app.accentColor.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = app.iconRes),
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }

            // Badge
            if (app.pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${app.pendingCount}",
                        color = Color.White,
                        fontSize = 11.sp,
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
            fontSize = 12.sp
        )
    }
}


