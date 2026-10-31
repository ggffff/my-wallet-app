package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.GlowBlue
import com.example.ui.theme.GlowPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * App Opening Splash Screen with a smooth, gentle ("Halka") logo animation
 * and seamless deep black backdrop matching the native window background.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Logo Design Reveal: smooth scale and alpha reveal with soft contact shadow
    val scale = remember { Animatable(0.88f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        try {
            // Smooth studio logo reveal
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            }
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(750, easing = FastOutSlowInEasing)
            )

            // Pleasant brief pause
            delay(400)
        } catch (t: Throwable) {
            android.util.Log.w("SplashScreen", "Animation interrupted", t)
        } finally {
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Luxury background matching AuthScreen
        Image(
            painter = painterResource(id = R.drawable.img_auth_luxury_bg),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Cosmic obsidian scrim & stardust particles
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to Color(0xFF030712).copy(alpha = 0.82f),
                    0.5f to Color(0xFF040A18).copy(alpha = 0.55f),
                    1.0f to Color(0xFF020409).copy(alpha = 0.94f)
                )
            )
            // Subtle stardust specks
            drawCircle(
                color = Color(0xFFD8E2FE).copy(alpha = 0.28f * alpha.value),
                radius = 1.8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.25f)
            )
            drawCircle(
                color = Color(0xFFD8E2FE).copy(alpha = 0.22f * alpha.value),
                radius = 1.4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.22f)
            )
            drawCircle(
                color = Color(0xFFD8E2FE).copy(alpha = 0.32f * alpha.value),
                radius = 2.0.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.68f)
            )
            drawCircle(
                color = Color(0xFFD8E2FE).copy(alpha = 0.25f * alpha.value),
                radius = 1.6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.14f, size.height * 0.75f)
            )
        }

        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // Clean unclipped 3D Wallet Logo
            Image(
                painter = painterResource(id = R.drawable.app_icon_fg),
                contentDescription = "Wallet Icon",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .testTag("splash_icon")
            )

            // Soft contact shadow (clean studio ground)
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 105.dp, height = 12.dp)
                    .scale(scale.value)
                    .alpha(alpha.value * 0.35f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF000000),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}
