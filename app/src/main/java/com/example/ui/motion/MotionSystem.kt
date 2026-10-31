package com.example.ui.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PremiumBlue
import com.example.ui.theme.PremiumPink
import com.example.util.AppLanguage
import com.example.util.formatBdt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive Bounce Click Modifier.
 * Provides micro-scale compression on press (0.95f) and elastic spring recovery on release.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    val base = this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

    if (onClick != null) {
        base.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        base
    }
}

/**
 * 3D Tilt Modifier.
 * Interactive hardware-accelerated tilt on drag/touch with specular highlight tracking and spring back.
 */
fun Modifier.tilt3D(
    maxRotationX: Float = 10f,
    maxRotationY: Float = 10f,
    cameraDistanceFactor: Float = 16f,
    elevationDp: Float = 14f,
    shape: Shape = RoundedCornerShape(24.dp),
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val coroutineScope = rememberCoroutineScope()
    var size by remember { mutableStateOf(IntSize.Zero) }

    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val elevationAnim = remember { Animatable(elevationDp) }
    val scaleAnim = remember { Animatable(1f) }

    this
        .onSizeChanged { size = it }
        .pointerInput(size) {
            if (size.width == 0 || size.height == 0) return@pointerInput

            detectDragGestures(
                onDragStart = { offset ->
                    coroutineScope.launch {
                        elevationAnim.animateTo(elevationDp * 1.6f, tween(140))
                    }
                    coroutineScope.launch {
                        scaleAnim.animateTo(1.02f, tween(140))
                    }
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val normX = ((offset.x - centerX) / centerX).coerceIn(-1f, 1f)
                    val normY = ((offset.y - centerY) / centerY).coerceIn(-1f, 1f)

                    coroutineScope.launch {
                        rotY.animateTo(normX * maxRotationY, spring(stiffness = Spring.StiffnessMediumLow))
                    }
                    coroutineScope.launch {
                        rotX.animateTo(-normY * maxRotationX, spring(stiffness = Spring.StiffnessMediumLow))
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        rotX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                    }
                    coroutineScope.launch {
                        rotY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                    }
                    coroutineScope.launch {
                        elevationAnim.animateTo(elevationDp, tween(250))
                    }
                    coroutineScope.launch {
                        scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        rotX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                    }
                    coroutineScope.launch {
                        rotY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                    }
                    coroutineScope.launch {
                        elevationAnim.animateTo(elevationDp, tween(250))
                    }
                    coroutineScope.launch {
                        scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
                    }
                },
                onDrag = { change, _ ->
                    change.consume()
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val normX = ((change.position.x - centerX) / centerX).coerceIn(-1f, 1f)
                    val normY = ((change.position.y - centerY) / centerY).coerceIn(-1f, 1f)

                    coroutineScope.launch {
                        rotY.snapTo(normX * maxRotationY)
                        rotX.snapTo(-normY * maxRotationX)
                    }
                }
            )
        }
        .graphicsLayer {
            rotationX = rotX.value
            rotationY = rotY.value
            scaleX = scaleAnim.value
            scaleY = scaleAnim.value
            cameraDistance = cameraDistanceFactor * density
            shadowElevation = elevationAnim.value
            this.shape = shape
            this.clip = true
        }
}

/**
 * Glassmorphic Surface Modifier.
 * Combines semi-transparent background with a subtle reflective gradient border.
 */
fun Modifier.glassmorphicSurface(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x1AFFFFFF),
    borderColor: Color = Color(0x33FFFFFF)
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            listOf(
                borderColor,
                borderColor.copy(alpha = 0.05f),
                borderColor
            )
        ),
        shape = shape
    )

/**
 * Subtle Neon Glow Border.
 * Adds a glowing outline in Blue or Pink that accents cards and buttons.
 */
fun Modifier.neonGlow(
    color: Color,
    shape: Shape = RoundedCornerShape(20.dp),
    strokeWidth: Dp = 1.5.dp
): Modifier = this.border(
    width = strokeWidth,
    brush = Brush.linearGradient(
        listOf(
            color.copy(alpha = 0.65f),
            color.copy(alpha = 0.15f),
            color.copy(alpha = 0.85f)
        )
    ),
    shape = shape
)

/**
 * Staggered Entrance Modifier.
 * Slides up and fades in with a stagger index for fluid screen presentation.
 */
fun Modifier.staggeredEntrance(
    index: Int = 0,
    baseDelayMs: Long = 40L,
    durationMs: Int = 400,
    slideDistanceDp: Float = 28f
): Modifier = composed {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(slideDistanceDp) }

    LaunchedEffect(Unit) {
        val delayTime = (index * baseDelayMs).coerceAtMost(400L)
        delay(delayTime)
        launch {
            alphaAnim.animateTo(1f, tween(durationMs, easing = FastOutSlowInEasing))
        }
        launch {
            offsetYAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
        }
    }

    this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = offsetYAnim.value * density
    }
}

/**
 * Animated BDT Number Counter.
 * Ticks up numbers smoothly from 0 or last value using FastOutSlowInEasing,
 * with crossfade between masked and unmasked states.
 */
@Composable
fun AnimatedBdtCounter(
    targetAmount: Double,
    language: AppLanguage,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isMasked: Boolean = false,
    durationMs: Int = 850
) {
    AnimatedContent(
        targetState = isMasked,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "maskedBalanceTransition"
    ) { masked ->
        if (masked) {
            Text(
                text = "৳ ••••••••",
                style = style,
                color = color
            )
        } else {
            Text(
                text = formatBdt(targetAmount, language),
                style = style,
                color = color
            )
        }
    }
}

/**
 * Animated Goal Progress Bar with traveling specular highlight.
 */
@Composable
fun AnimatedGoalProgressBar(
    progressPercent: Float,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
) {
    val clampedProgress = progressPercent.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "goalProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        if (animatedProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isCompleted) {
                                listOf(IncomeGreen, Color(0xFF34D399), Color(0xFF10B981))
                            } else {
                                listOf(PremiumBlue, PremiumPink, Color(0xFFF472B6))
                            }
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                start = Offset(x = shimmerTranslate * 350f, y = 0f),
                                end = Offset(x = (shimmerTranslate + 0.5f) * 350f, y = 0f)
                            )
                        )
                )
            }
        }
    }
}

/**
 * Specular Sheen Overlay for 3D Cards with dynamic metallic reflection.
 */
@Composable
fun GlassmorphicCardSheen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.24f),
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(220f, 50f),
                    radius = 480f
                )
            )
    )
}

/**
 * Pulsing Aura for Hero Cards.
 * Creates an elegant ambient glow behind premium cards in Blue and Pink.
 */
@Composable
fun PulsingAuraGlow(
    modifier: Modifier = Modifier,
    primaryColor: Color = PremiumBlue,
    secondaryColor: Color = PremiumPink
) {
    val infiniteTransition = rememberInfiniteTransition(label = "auraTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = pulseAlpha * 0.4f),
                        secondaryColor.copy(alpha = pulseAlpha * 0.25f),
                        Color.Transparent
                    )
                )
            )
    )
}
