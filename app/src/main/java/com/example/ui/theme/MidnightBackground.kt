package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// =========================================================================
// Premium Midnight Blue Banking Theme Palette
// Replaces completely black (#000000) with Deep Navy, Midnight Blue & Indigo
// =========================================================================
val MidnightNavyDark = Color(0xFF060D1E)       // Deepest background navy
val MidnightNavyMid = Color(0xFF09142E)        // Midnight blue mid-tone
val MidnightIndigoDepth = Color(0xFF0D183B)    // Subtle indigo atmospheric depth
val MidnightNavyBottom = Color(0xFF070E22)     // Grounded base navy

val MidnightCardSurface = Color(0xFF0F1E3D)    // Slightly lighter navy for cards
val MidnightCardElevated = Color(0xFF15284F)   // Elevated cards / chip containers
val MidnightCardBorder = Color(0xFF1E3566)     // Subtle crisp card outline
val MidnightCyanHighlight = Color(0xFF38BDF8)  // Subtle cyan highlight (non-aggressive)
val MidnightRoyalBlue = Color(0xFF2563EB)      // Classy fintech royal blue

private val LiteBackgroundColor = Color(0xFFF4F7FF)

/**
 * Sophisticated Premium Midnight Blue Theme Background for Dark Mode.
 *
 * Uses [Modifier.drawWithCache] so all brushes, paths, and strokes are allocated
 * once per layout size and reused across every scroll frame with zero GC pressure.
 */
fun Modifier.midnightAppBackground(isDark: Boolean): Modifier = this.then(
    if (isDark) {
        Modifier.drawWithCache {
            val w = size.width
            val h = size.height

            val baseGradient = Brush.verticalGradient(
                colors = listOf(
                    MidnightNavyDark,
                    MidnightNavyMid,
                    MidnightIndigoDepth,
                    MidnightNavyBottom
                )
            )

            val topOrbBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = 0.15f),
                    Color(0xFF0052FE).copy(alpha = 0.07f),
                    Color.Transparent
                ),
                center = Offset(w * 0.88f, h * 0.10f),
                radius = (w * 0.72f).coerceAtLeast(1f)
            )

            val midOrbBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4338CA).copy(alpha = 0.11f),
                    Color(0xFF1E1B4B).copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(w * 0.10f, h * 0.48f),
                radius = (w * 0.65f).coerceAtLeast(1f)
            )

            val bottomOrbBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0369A1).copy(alpha = 0.09f),
                    Color.Transparent
                ),
                center = Offset(w * 0.82f, h * 0.86f),
                radius = (w * 0.58f).coerceAtLeast(1f)
            )

            val wave1 = Path().apply {
                moveTo(0f, h * 0.22f)
                cubicTo(w * 0.28f, h * 0.16f, w * 0.68f, h * 0.28f, w, h * 0.19f)
                lineTo(w, h * 0.27f)
                cubicTo(w * 0.65f, h * 0.34f, w * 0.32f, h * 0.23f, 0f, h * 0.28f)
                close()
            }
            val wave1Brush = Brush.horizontalGradient(
                colors = listOf(
                    MidnightCyanHighlight.copy(alpha = 0.035f),
                    Color(0xFF0284C7).copy(alpha = 0.055f),
                    Color(0xFF6366F1).copy(alpha = 0.035f)
                )
            )

            val contour1 = Path().apply {
                moveTo(0f, h * 0.46f)
                cubicTo(w * 0.35f, h * 0.42f, w * 0.70f, h * 0.53f, w, h * 0.47f)
            }
            val contour1Brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = 0.02f),
                    MidnightCyanHighlight.copy(alpha = 0.08f),
                    Color(0xFF818CF8).copy(alpha = 0.035f)
                )
            )
            val contour1Stroke = Stroke(width = 1.4.dp.toPx())

            val contour2 = Path().apply {
                moveTo(0f, h * 0.72f)
                cubicTo(w * 0.30f, h * 0.77f, w * 0.68f, h * 0.67f, w, h * 0.73f)
            }
            val contour2Brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF6366F1).copy(alpha = 0.05f),
                    MidnightRoyalBlue.copy(alpha = 0.07f),
                    Color(0xFF06B6D4).copy(alpha = 0.025f)
                )
            )
            val contour2Stroke = Stroke(width = 1.2.dp.toPx())

            val lineY1 = h * 0.36f
            val lineY2 = h * 0.64f
            val line1Brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    MidnightCyanHighlight.copy(alpha = 0.035f),
                    Color(0xFF0284C7).copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
            val line2Brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    Color(0xFF818CF8).copy(alpha = 0.035f),
                    Color.Transparent
                )
            )

            onDrawBehind {
                drawRect(brush = baseGradient)
                drawCircle(brush = topOrbBrush)
                drawCircle(brush = midOrbBrush)
                drawCircle(brush = bottomOrbBrush)
                drawPath(path = wave1, brush = wave1Brush)
                drawPath(path = contour1, brush = contour1Brush, style = contour1Stroke)
                drawPath(path = contour2, brush = contour2Brush, style = contour2Stroke)
                drawLine(
                    brush = line1Brush,
                    start = Offset(w * 0.1f, lineY1),
                    end = Offset(w * 0.9f, lineY1),
                    strokeWidth = 1f
                )
                drawLine(
                    brush = line2Brush,
                    start = Offset(w * 0.2f, lineY2),
                    end = Offset(w * 0.8f, lineY2),
                    strokeWidth = 1f
                )
            }
        }
    } else {
        Modifier.drawWithCache {
            onDrawBehind {
                drawRect(LiteBackgroundColor)
            }
        }
    }
)
