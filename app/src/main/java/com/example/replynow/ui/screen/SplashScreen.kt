package com.example.replynow.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.replynow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val iconScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Icon enters with a bounce-scale + fade
        launch {
            iconAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            iconScale.animateTo(1.15f, tween(500, easing = FastOutSlowInEasing))
            iconScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        }
        // Glow ring pulses in
        launch {
            delay(250)
            glowAlpha.animateTo(0.5f, tween(500))
            glowAlpha.animateTo(0.15f, tween(600))
        }
        // Title fades in after icon
        launch {
            delay(450)
            textAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        // Tagline fades in last
        launch {
            delay(700)
            taglineAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        // Hold, then navigate
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glow ring behind icon
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(glowAlpha.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF97F21).copy(alpha = 0.4f),
                                    Color(0xFFF97F21).copy(alpha = 0.0f)
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.replynow_icon),
                    contentDescription = "ReplyNow",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(iconScale.value)
                        .alpha(iconAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "ReplyNow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF97F21),
                modifier = Modifier.alpha(textAlpha.value),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Never forget to reply",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8C6239).copy(alpha = 0.7f),
                modifier = Modifier.alpha(taglineAlpha.value),
                letterSpacing = 0.5.sp
            )
        }
    }
}
