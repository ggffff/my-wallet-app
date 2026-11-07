package com.example.ui.auth

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.R
import com.example.ui.dialogs.CountryLanguageDialog
import com.example.ui.theme.LocalAppTheme
import com.example.util.AppLanguage
import com.example.util.AppSignatureHelper
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.sin

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private data class BackgroundParticle(
    val xRatio: Float,
    val yRatio: Float,
    val sizeDp: Float,
    val isDiamond: Boolean,
    val speed: Float,
    val phase: Float
)

private val particles = listOf(
    BackgroundParticle(0.10f, 0.16f, 16f, true, 0.8f, 0.2f),
    BackgroundParticle(0.85f, 0.14f, 14f, true, 0.9f, 1.4f),
    BackgroundParticle(0.08f, 0.38f, 12f, true, 0.7f, 2.6f),
    BackgroundParticle(0.92f, 0.36f, 14f, true, 1.1f, 3.2f),
    BackgroundParticle(0.38f, 0.92f, 16f, true, 0.85f, 4.0f),
    BackgroundParticle(0.96f, 0.76f, 18f, true, 0.95f, 5.1f),
    BackgroundParticle(0.05f, 0.88f, 10f, true, 0.75f, 0.9f),
    BackgroundParticle(0.42f, 0.12f, 4f, false, 0.6f, 1.8f),
    BackgroundParticle(0.88f, 0.26f, 5f, false, 0.8f, 3.5f),
    BackgroundParticle(0.09f, 0.48f, 4.5f, false, 0.7f, 4.8f),
    BackgroundParticle(0.93f, 0.58f, 5.5f, false, 1.0f, 2.2f),
    BackgroundParticle(0.18f, 0.74f, 4f, false, 0.75f, 0.5f),
    BackgroundParticle(0.82f, 0.82f, 5f, false, 0.9f, 3.8f)
)

/**
 * Premium, minimal fintech Login Page strictly adhering to the user reference design.
 * Includes ONLY:
 * - Top-right Light/Dark mode toggle (Sun / Moon)
 * - My Wallet 3D App Logo with glowing orbital pedestal
 * - App Name: "My Wallet" ("My" in white/dark slate, "Wallet" in electric cyan/blue)
 * - Tagline: "Your Smart Personal Finance Companion"
 * - Official "Sign in with Google" pill button (with vertical divider)
 * - "OR" divider line
 * - "Continue as Guest" pill button with avatar icon
 * - Lightweight, battery-friendly animated background waves & glowing particles
 */
@Composable
fun AuthScreen(
    currentLanguage: AppLanguage = LocalAppLanguage.current,
    onAuthSuccess: () -> Unit,
    onExploreAsGuest: () -> Unit = {},
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onToggleLanguage: (AppLanguage) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    var showCountryDialog by remember { mutableStateOf(false) }

    val isDark = LocalAppTheme.current.isDark
    val scrollState = rememberScrollState()

    // Smooth, battery-friendly ambient animations for background waves and particles
    val infiniteTransition = rememberInfiniteTransition(label = "auth_bg_transition")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val particleDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_drift"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen"),
        color = if (isDark) Color(0xFF030718) else Color(0xFFF8FAFC)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // =========================================================================
            // 1. LIGHTWEIGHT ANIMATED BACKGROUND (Waves, Glows & Soft Floating Particles)
            // =========================================================================
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Base Background Gradient
                val baseGradient = if (isDark) {
                    Brush.verticalGradient(
                        0.0f to Color(0xFF020617),
                        0.35f to Color(0xFF050E28),
                        0.70f to Color(0xFF03081C),
                        1.0f to Color(0xFF010410)
                    )
                } else {
                    Brush.verticalGradient(
                        0.0f to Color(0xFFF8FAFC),
                        0.40f to Color(0xFFEFF6FF),
                        0.80f to Color(0xFFF1F5F9),
                        1.0f to Color(0xFFE2E8F0)
                    )
                }
                drawRect(brush = baseGradient)

                // Soft Corner Glow Accents
                if (isDark) {
                    // Top-Left Blue Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF0070F3).copy(alpha = 0.22f * glowPulse), Color.Transparent),
                            center = Offset(w * 0.05f, h * 0.08f),
                            radius = w * 0.55f
                        ),
                        radius = w * 0.55f,
                        center = Offset(w * 0.05f, h * 0.08f)
                    )
                    // Top-Right Cyan Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00D2FF).copy(alpha = 0.18f * glowPulse), Color.Transparent),
                            center = Offset(w * 0.92f, h * 0.12f),
                            radius = w * 0.50f
                        ),
                        radius = w * 0.50f,
                        center = Offset(w * 0.92f, h * 0.12f)
                    )
                    // Bottom-Right Ambient Wave Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF0055FE).copy(alpha = 0.28f * glowPulse), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.92f),
                            radius = w * 0.65f
                        ),
                        radius = w * 0.65f,
                        center = Offset(w * 0.85f, h * 0.92f)
                    )
                    // Bottom-Left Cyan Wave Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.20f * glowPulse), Color.Transparent),
                            center = Offset(w * 0.12f, h * 0.95f),
                            radius = w * 0.55f
                        ),
                        radius = w * 0.55f,
                        center = Offset(w * 0.12f, h * 0.95f)
                    )
                } else {
                    // Soft light-mode blue glows
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF93C5FD).copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(w * 0.10f, h * 0.10f),
                            radius = w * 0.50f
                        ),
                        radius = w * 0.50f,
                        center = Offset(w * 0.10f, h * 0.10f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(w * 0.90f, h * 0.90f),
                            radius = w * 0.55f
                        ),
                        radius = w * 0.55f,
                        center = Offset(w * 0.90f, h * 0.90f)
                    )
                }

                // Smooth Flowing Curves (Wave Ribbons matching reference)
                val waveShift = sin(wavePhase.toDouble()).toFloat() * 14f

                // Top Wave Ribbon
                val topWave = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.18f + waveShift)
                    cubicTo(
                        w * 0.65f, h * 0.24f - waveShift,
                        w * 0.35f, h * 0.08f + waveShift,
                        0f, h * 0.16f - waveShift
                    )
                    close()
                }
                drawPath(
                    path = topWave,
                    brush = Brush.linearGradient(
                        colors = if (isDark) listOf(
                            Color(0xFF0070F3).copy(alpha = 0.22f),
                            Color(0xFF00E5FF).copy(alpha = 0.12f),
                            Color.Transparent
                        ) else listOf(
                            Color(0xFF93C5FD).copy(alpha = 0.22f),
                            Color(0xFFBAE6FD).copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )

                // Bottom Wave Ribbon
                val bottomWave = Path().apply {
                    moveTo(0f, h)
                    lineTo(w, h)
                    lineTo(w, h * 0.88f - waveShift)
                    cubicTo(
                        w * 0.70f, h * 0.80f + waveShift,
                        w * 0.30f, h * 0.96f - waveShift,
                        0f, h * 0.86f + waveShift
                    )
                    close()
                }
                drawPath(
                    path = bottomWave,
                    brush = Brush.linearGradient(
                        colors = if (isDark) listOf(
                            Color(0xFF0052D4).copy(alpha = 0.32f),
                            Color(0xFF00D2FF).copy(alpha = 0.20f),
                            Color.Transparent
                        ) else listOf(
                            Color(0xFF60A5FA).copy(alpha = 0.20f),
                            Color(0xFF93C5FD).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )

                // Floating Glowing Stardust & Diamond Particles
                particles.forEach { p ->
                    val progress = (particleDrift * p.speed + p.phase) % 1.0f
                    val y = ((p.yRatio - progress * 0.08f) % 1.0f + 1.0f) % 1.0f * h
                    val x = p.xRatio * w + sin((progress * 6.28f + p.phase).toDouble()).toFloat() * 6f
                    val alpha = (0.25f + 0.35f * sin((progress * 6.28f + p.phase).toDouble()).toFloat()).coerceIn(0.10f, 0.65f)

                    val particleColor = if (isDark) {
                        Color(0xFF60A5FA).copy(alpha = alpha)
                    } else {
                        Color(0xFF3B82F6).copy(alpha = alpha * 0.75f)
                    }

                    if (p.isDiamond) {
                        val dSize = p.sizeDp.dp.toPx()
                        rotate(degrees = 45f, pivot = Offset(x, y)) {
                            drawRoundRect(
                                color = particleColor,
                                topLeft = Offset(x - dSize / 2f, y - dSize / 2f),
                                size = androidx.compose.ui.geometry.Size(dSize, dSize),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    } else {
                        drawCircle(
                            color = particleColor,
                            radius = (p.sizeDp / 2f).dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            // =========================================================================
            // 2. FOREGROUND: CLEAN MINIMAL FINTECH CONTENT (Strictly as per Reference)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Language Selector + Compact Theme Toggle (Sun / Moon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 2.dp, end = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country & Language Selector Pill
                    Surface(
                        onClick = { showCountryDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF0F1E40).copy(alpha = 0.90f) else Color.White,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFCBD5E1)),
                        shadowElevation = if (isDark) 0.dp else 2.dp,
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("auth_language_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = currentLanguage.flagEmoji, fontSize = 18.sp)
                            Text(
                                text = currentLanguage.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                    }

                    // Theme Toggle
                    Surface(
                        onClick = { onToggleDarkTheme(!isDark) },
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF0F1E40).copy(alpha = 0.90f) else Color.White,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFCBD5E1)),
                        shadowElevation = if (isDark) 0.dp else 3.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("auth_theme_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF1E293B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Center Block: 3D App Logo, "My Wallet" Title, Tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 3D Wallet Logo with glowing neon orbital pedestal ring matching reference
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        // Glowing neon orbital pedestal rings
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            val cx = size.width / 2f
                            val cy = size.height * 0.52f
                            val outerRadiusX = 135.dp.toPx()
                            val outerRadiusY = 26.dp.toPx()
                            val innerRadiusX = 102.dp.toPx()
                            val innerRadiusY = 19.dp.toPx()

                            if (isDark) {
                                // Soft intense neon blue radial diffuse glow
                                drawOval(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF).copy(alpha = 0.45f * glowPulse),
                                            Color(0xFF0070F3).copy(alpha = 0.25f * glowPulse),
                                            Color.Transparent
                                        ),
                                        center = Offset(cx, cy),
                                        radius = outerRadiusX * 1.15f
                                    ),
                                    topLeft = Offset(cx - outerRadiusX * 1.1f, cy - outerRadiusY * 1.4f),
                                    size = androidx.compose.ui.geometry.Size(outerRadiusX * 2.2f, outerRadiusY * 2.8f)
                                )

                                // Outer crisp neon ellipse ring
                                drawOval(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF0070F3).copy(alpha = 0.3f),
                                            Color(0xFF00E5FF).copy(alpha = 0.95f),
                                            Color(0xFF00D2FF).copy(alpha = 0.95f),
                                            Color(0xFF0070F3).copy(alpha = 0.3f)
                                        ),
                                        startX = cx - outerRadiusX,
                                        endX = cx + outerRadiusX
                                    ),
                                    topLeft = Offset(cx - outerRadiusX, cy - outerRadiusY),
                                    size = androidx.compose.ui.geometry.Size(outerRadiusX * 2f, outerRadiusY * 2f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                                )

                                // Inner bright neon ellipse ring
                                drawOval(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF0052FE).copy(alpha = 0.2f),
                                            Color(0xFF38BDF8).copy(alpha = 0.90f),
                                            Color(0xFF00E5FF).copy(alpha = 0.90f),
                                            Color(0xFF0052FE).copy(alpha = 0.2f)
                                        ),
                                        startX = cx - innerRadiusX,
                                        endX = cx + innerRadiusX
                                    ),
                                    topLeft = Offset(cx - innerRadiusX, cy - innerRadiusY),
                                    size = androidx.compose.ui.geometry.Size(innerRadiusX * 2f, innerRadiusY * 2f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            } else {
                                // Soft light mode pedestal shadow & gentle blue ring
                                drawOval(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF60A5FA).copy(alpha = 0.25f),
                                            Color.Transparent
                                        ),
                                        center = Offset(cx, cy),
                                        radius = outerRadiusX
                                    ),
                                    topLeft = Offset(cx - outerRadiusX, cy - outerRadiusY),
                                    size = androidx.compose.ui.geometry.Size(outerRadiusX * 2f, outerRadiusY * 2f)
                                )
                                drawOval(
                                    color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                                    topLeft = Offset(cx - innerRadiusX, cy - innerRadiusY),
                                    size = androidx.compose.ui.geometry.Size(innerRadiusX * 2f, innerRadiusY * 2f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            }
                        }

                        Image(
                            painter = painterResource(id = R.drawable.img_wallet_3d_logo),
                            contentDescription = "My Wallet 3D Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(190.dp)
                                .testTag("my_wallet_3d_logo")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // App Name: Dynamic localization (e.g. "My Wallet" or "আমার ওয়ালেট")
                    Text(
                        text = appString(StringKeys.APP_NAME),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("app_title_text")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tagline
                    Text(
                        text = appString(StringKeys.TAGLINE),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            letterSpacing = 0.1.sp
                        ),
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("app_tagline")
                    )
                }

                Spacer(modifier = Modifier.height(42.dp))

                // Bottom Block: Sign in with Google -> OR divider -> Continue as Guest
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Sign in with Google Button (Official Google Logo + Vertical Divider)
                    Surface(
                        onClick = {
                            if (!isLoading) {
                                isLoading = true
                                signInWithGoogle(
                                    context = context,
                                    scope = coroutineScope,
                                    onSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorDialogMessage = err
                                    },
                                    onCancelled = {
                                        isLoading = false
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        enabled = !isLoading
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 22.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF2563EB),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = appString(StringKeys.SIGNING_IN),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .height(26.dp)
                                        .width(1.dp)
                                        .background(Color(0xFFE2E8F0))
                                )
                                Text(
                                    text = appString(StringKeys.SIGN_IN_WITH_GOOGLE),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A),
                                        letterSpacing = 0.2.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. OR Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1))
                        )
                        Text(
                            text = appString(StringKeys.OR),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.5.sp
                            ),
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1))
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Continue as Guest Button (Avatar Icon + Electric Blue Outline)
                    Surface(
                        onClick = onExploreAsGuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("continue_as_guest_button"),
                        shape = RoundedCornerShape(28.dp),
                        color = if (isDark) Color(0xFF0A132C).copy(alpha = 0.85f) else Color.White,
                        border = BorderStroke(2.dp, Color(0xFF0091FF)),
                        shadowElevation = if (isDark) 0.dp else 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 22.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isDark) Color.White else Color(0xFF0091FF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = appString(StringKeys.CONTINUE_GUEST),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = if (isDark) Color.White else Color(0xFF0091FF)
                            )
                        }
                    }
                }
            }
        }

        // Diagnostic / Error Dialog
        if (errorDialogMessage != null) {
            AlertDialog(
                onDismissRequest = { errorDialogMessage = null },
                title = {
                    Text(
                        text = "Sign-In Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        SelectionContainer {
                            Text(
                                text = errorDialogMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { errorDialogMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("OK", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Sign-in Diagnostics", errorDialogMessage)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied instructions to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Copy Details", color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            )
        }

        if (showCountryDialog) {
            CountryLanguageDialog(
                currentLanguage = currentLanguage,
                onDismiss = { showCountryDialog = false },
                onSelectLanguage = { newLang ->
                    onToggleLanguage(newLang)
                    showCountryDialog = false
                }
            )
        }
    }
}

internal fun getWebClientId(context: Context): String? {
    try {
        val rClass = Class.forName("com.example.R\$string")
        val field = rClass.getField("default_web_client_id")
        val resId = field.getInt(null)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) return clientId
        }
    } catch (_: Throwable) {}

    try {
        val rClass = Class.forName("${context.packageName}.R\$string")
        val field = rClass.getField("default_web_client_id")
        val resId = field.getInt(null)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) return clientId
        }
    } catch (_: Throwable) {}

    try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) return clientId
        }
    } catch (_: Throwable) {}

    try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", "com.example")
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) return clientId
        }
    } catch (_: Throwable) {}

    try {
        val resId = context.resources.getIdentifier("web_client_id", "string", context.packageName)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) return clientId
        }
    } catch (_: Throwable) {}

    return null
}

private fun signInWithGoogle(
    context: Context,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val activity = context.findActivity()
    if (activity == null) {
        Log.e("Auth", "Unable to resolve Activity from context")
        onError("Unable to launch Google Sign-In: Application activity not found.")
        return
    }

    val clientId = getWebClientId(context)
    if (clientId.isNullOrBlank()) {
        Log.e("Auth", "Web Client ID (default_web_client_id) not found")
        onError("Google Sign-In configuration missing: Web OAuth Client ID was not found in google-services resources. You can still use the app by tapping 'Continue as Guest'.")
        return
    }

    Log.d("Auth", "Using Web Client ID prefix: ${clientId.take(12)}... (valid serverClientId)")

    val credentialManager = try {
        CredentialManager.create(context)
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to initialize CredentialManager", t)
        onError("Google Sign-In service not available on this device: ${t.localizedMessage ?: t.javaClass.simpleName}")
        return
    }

    val signInOption = try {
        GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to create GetSignInWithGoogleOption", t)
        onError("Failed to initialize Google Sign-In options: ${t.localizedMessage}")
        return
    }

    val request = try {
        GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to create GetCredentialRequest", t)
        onError("Failed to create credential request: ${t.localizedMessage}")
        return
    }

    val targetScope = (activity as? ComponentActivity)?.lifecycleScope ?: scope
    targetScope.launch {
        try {
            Log.d("Auth", "Requesting credential from CredentialManager...")
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            Log.d("Auth", "Credential returned of type: ${credential.type}")

            val idToken: String? = when {
                credential is CustomCredential && (
                    credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                ) -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: Exception) {
                        credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                            ?: credential.data.getString("id_token")
                    }
                }
                credential is CustomCredential -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: Exception) {
                        credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                            ?: credential.data.getString("id_token")
                    }
                }
                else -> null
            }

            if (idToken.isNullOrBlank()) {
                Log.e("Auth", "Credential did not provide an ID token. Credential type: ${credential.type}")
                onError("Google account was selected, but no ID token was received. Please ensure Google Sign-In is enabled in Firebase Console.")
                return@launch
            }

            Log.d("Auth", "Google ID token obtained. Signing into Firebase...")
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = Firebase.auth.signInWithCredential(authCredential).await()
            val user = authResult.user ?: Firebase.auth.currentUser

            if (user != null) {
                Log.d("Auth", "Firebase authentication succeeded for user: ${user.uid}")
                onSuccess()
            } else {
                Log.e("Auth", "signInWithCredential returned null user")
                onError("Authentication succeeded, but Firebase user profile is null.")
            }
        } catch (e: GetCredentialCancellationException) {
            val rawMsg = e.message ?: "Sign-in dismissed"
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: $rawMsg", e)
            val runtimeSig = AppSignatureHelper.getAppSignature(context)
            val currentSha1 = runtimeSig?.first ?: AppSignatureHelper.EXPECTED_SHA1
            val currentSha256 = runtimeSig?.second ?: AppSignatureHelper.EXPECTED_SHA256
            val matchesExpected = currentSha1.equals(AppSignatureHelper.EXPECTED_SHA1, ignoreCase = true)

            if (rawMsg.contains("USER_CANCELED", ignoreCase = true) || (rawMsg.contains("cancel", ignoreCase = true) && !rawMsg.contains("10"))) {
                onCancelled()
                return@launch
            }

            val detailMsg = buildString {
                append("Sign-in bottom sheet was closed or dismissed ($rawMsg).\n\n")
                if (!matchesExpected) {
                    append("⚠️ SIGNING KEY MISMATCH DETECTED:\n")
                    append("This installed APK was signed with:\nSHA-1: $currentSha1\n\n")
                    append("Expected in google-services.json:\nSHA-1: ${AppSignatureHelper.EXPECTED_SHA1}\n\n")
                    append("Action Required: Register this APK's SHA-1 in Firebase Console under Project Settings.\n")
                } else {
                    append("Tip: If running on an emulator without a configured Google account, tap 'Continue as Guest' to explore the app directly.\n\n")
                    append("To enable Google Sign-In in Firebase Console:\n")
                    append("1. Open Firebase Console -> Project Settings -> Your apps -> com.aistudio.mywallet.bdtnas\n")
                    append("2. Add SHA-1:\n   $currentSha1\n")
                    append("3. In Authentication -> Sign-in method, ensure Google is Enabled.\n")
                }
            }
            onError(detailMsg)
        } catch (e: CancellationException) {
            Log.d("Auth", "Google Sign-In coroutine cleanly finished or cancelled: ${e.message}")
        } catch (e: GetCredentialException) {
            Log.e("Auth", "GetCredentialException during Google Sign-In", e)
            val rawMsg = e.message ?: e.javaClass.simpleName
            val friendlyMsg = when {
                rawMsg.contains("10") || rawMsg.contains("DeveloperError") ->
                    "Google Play Services Developer Error (10): APK signing certificate not recognized in Firebase Console.\n\nYou can tap 'Continue as Guest' to use the app immediately!"
                rawMsg.contains("16") || rawMsg.contains("Cannot find matching credential") ->
                    "Google Sign-In configuration error: Ensure Google provider is enabled in Firebase Authentication console."
                rawMsg.contains("NoCredentialException") ->
                    "No Google account found on this device. Tap 'Continue as Guest' to explore without login."
                else ->
                    "Google Sign-In note: $rawMsg\n(You can tap 'Continue as Guest' to enter directly)"
            }
            onError(friendlyMsg)
        } catch (e: FirebaseNetworkException) {
            Log.e("Auth", "Firebase network error", e)
            onError("Network error: Could not reach Firebase servers. Please check your internet connection or use Guest Mode.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("Auth", "Invalid Firebase credentials", e)
            onError("Authentication error: The Google ID token could not be verified by Firebase.")
        } catch (e: FirebaseAuthException) {
            Log.e("Auth", "Firebase Auth error [${e.errorCode}]", e)
            onError("Firebase Authentication error (${e.errorCode}): ${e.localizedMessage ?: "Please try again or use Guest Mode"}")
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Log.e("Auth", "Unexpected error during Google Sign-In", t)
            onError("Sign in note: ${t.localizedMessage ?: t.javaClass.simpleName}")
        }
    }
}
