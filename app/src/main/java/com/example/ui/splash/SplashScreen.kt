package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BrandGlow
import com.example.ui.theme.DarkBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val glowAlpha = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {
        glowAlpha.animateTo(
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        try {
            scale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
            delay(500)
        } catch (t: Throwable) {
            android.util.Log.w("SplashScreen", "Animation interrupted", t)
        } finally {
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Glowing animated aura
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(scale.value * 1.15f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandGlow.copy(alpha = glowAlpha.value * 0.5f),
                            Color(0xFF0070F3).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Wallet App Icon with gentle elevation and rounded corners
        Image(
            painter = painterResource(id = R.drawable.app_icon_fg),
            contentDescription = "Wallet Icon",
            modifier = Modifier
                .size(136.dp)
                .scale(scale.value)
                .shadow(24.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .testTag("splash_icon")
        )
    }
}
