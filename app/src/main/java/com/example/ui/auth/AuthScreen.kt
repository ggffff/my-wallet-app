package com.example.ui.auth

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.ui.theme.BrandGlow
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.GlowBlue
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PureWhite
import com.example.util.AppSignatureHelper
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class UniversalStar(
    val xRatio: Float,
    val yRatio: Float,
    val sizeDp: Float,
    val baseAlpha: Float,
    val pulseRange: Float,
    val speedFactor: Float,
    val phase: Float
)

private val universalStars = listOf(
    UniversalStar(0.12f, 0.15f, 1.6f, 0.22f, 0.12f, 0.8f, 0.3f),
    UniversalStar(0.28f, 0.08f, 2.2f, 0.28f, 0.14f, 1.1f, 1.2f),
    UniversalStar(0.85f, 0.12f, 1.4f, 0.20f, 0.10f, 0.7f, 2.4f),
    UniversalStar(0.72f, 0.22f, 2.0f, 0.26f, 0.13f, 1.0f, 0.9f),
    UniversalStar(0.06f, 0.32f, 1.2f, 0.18f, 0.09f, 0.6f, 3.1f),
    UniversalStar(0.92f, 0.36f, 1.8f, 0.24f, 0.12f, 0.9f, 4.2f),
    UniversalStar(0.18f, 0.44f, 1.5f, 0.21f, 0.11f, 0.75f, 5.0f),
    UniversalStar(0.82f, 0.48f, 2.4f, 0.30f, 0.15f, 1.2f, 1.7f),
    UniversalStar(0.10f, 0.58f, 1.3f, 0.18f, 0.10f, 0.65f, 2.8f),
    UniversalStar(0.88f, 0.62f, 1.9f, 0.25f, 0.13f, 0.95f, 3.9f),
    UniversalStar(0.22f, 0.70f, 1.7f, 0.23f, 0.12f, 0.85f, 0.5f),
    UniversalStar(0.76f, 0.74f, 2.1f, 0.27f, 0.14f, 1.05f, 2.1f),
    UniversalStar(0.08f, 0.82f, 1.4f, 0.19f, 0.10f, 0.7f, 4.6f),
    UniversalStar(0.90f, 0.86f, 1.6f, 0.22f, 0.11f, 0.8f, 1.4f),
    UniversalStar(0.35f, 0.92f, 2.0f, 0.26f, 0.13f, 1.0f, 3.3f),
    UniversalStar(0.65f, 0.94f, 1.5f, 0.20f, 0.11f, 0.75f, 5.8f),
    UniversalStar(0.48f, 0.18f, 1.8f, 0.25f, 0.12f, 0.9f, 0.8f),
    UniversalStar(0.55f, 0.80f, 1.6f, 0.21f, 0.11f, 0.85f, 2.7f)
)

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("Auth", "Failed to copy to clipboard", e)
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    val density = LocalDensity.current

    // =========================================================================
    // LOGO DESIGN MOTION SYSTEM
    // Subtle, minimalist ("Sample"), professional studio logo reveal & idle motion.
    // Zero harsh highlights, zero bright glaring sweeps, zero bounding circles.
    // =========================================================================

    // 1. Studio Logo Reveal (Smooth entrance on load)
    val entranceScale = remember { Animatable(0.88f) }
    val entranceAlpha = remember { Animatable(0f) }
    val entranceTiltX = remember { Animatable(10f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        // Logo reveals with smooth eased curve
        launch {
            entranceAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }
        launch {
            entranceScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 950, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }
        launch {
            entranceTiltX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 950, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }

        // Title reveals in staggered coordination
        delay(250)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
    }

    // 2. Idle Ambient Logo Float & Universal Style Motion System
    val logoMotion = rememberInfiniteTransition(label = "logo_motion_transition")

    // Universal Style: Slow continuous stardust drift cycle (16s smooth loop)
    val celestialCycle by logoMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "celestial_cycle"
    )

    // Universal Style: Celestial orbital trajectory cycle (11s gentle 360-degree orbit)
    val orbitProgress by logoMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_progress"
    )

    // Slow, silky levitation (3.8s calm breath cycle)
    val floatY by logoMotion.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float_y"
    )

    // Micro 3D perspective pitch (giving physical studio weight)
    val tiltY by logoMotion.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_tilt_y"
    )

    val tiltZ by logoMotion.animateFloat(
        initialValue = -0.8f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_tilt_z"
    )

    // Synchronized studio contact shadow parameters
    val normalizedFloat = (floatY + 5f) / 10f // 0f at apex, 1f at lowest
    val shadowScaleX = 0.94f + (0.10f * (1f - normalizedFloat))
    val shadowAlpha = 0.22f + (0.14f * normalizedFloat)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen"),
        color = Color(0xFF030712) // Deepest luxury obsidian
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // =========================================================================
            // 1. BESPOKE LUXURY FINTECH BACKGROUND (Universal Style Cosmic Stardust, No Harsh Highlights)
            // =========================================================================
            Image(
                painter = painterResource(id = R.drawable.img_auth_luxury_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Deep luxury atmospheric vignette & subtle universal stardust particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Pure Obsidian Scrim: Keeps the background calm and non-distracting
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color(0xFF030712).copy(alpha = 0.78f),
                        0.25f to Color(0xFF040A18).copy(alpha = 0.50f),
                        0.60f to Color(0xFF040814).copy(alpha = 0.55f),
                        1.0f to Color(0xFF020409).copy(alpha = 0.94f)
                    )
                )

                // Soft, ultra-subtle ambient studio vignette (No bright highlight or hotspot)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF020408).copy(alpha = 0.40f),
                            Color(0xFF010205).copy(alpha = 0.70f)
                        ),
                        center = Offset(w * 0.5f, h * 0.45f),
                        radius = w * 0.85f
                    )
                )

                // Universal Style: Gentle stardust drifting across deep space ("Halka" & calm)
                universalStars.forEach { star ->
                    val starCycle = (celestialCycle + star.phase / (2 * PI.toFloat())) % 1.0f
                    val starY = ((star.yRatio - starCycle * star.speedFactor * 0.10f) % 1.0f + 1.0f) % 1.0f * h
                    val starX = star.xRatio * w
                    val starAlpha = (star.baseAlpha + star.pulseRange * sin((celestialCycle * 2 * PI.toFloat() + star.phase))).coerceIn(0.06f, 0.38f)
                    val starRadius = star.sizeDp.dp.toPx()

                    drawCircle(
                        color = Color(0xFFD8E2FE).copy(alpha = starAlpha),
                        radius = starRadius,
                        center = Offset(starX, starY)
                    )
                }
            }

            // =========================================================================
            // 2. FOREGROUND: Logo Design Presentation (Universal Orbit -> 3D Logo -> App Name -> Sign In)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(start = 28.dp, end = 28.dp, top = 56.dp, bottom = 125.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top balance spacer
                Spacer(modifier = Modifier.height(24.dp))

                // Center: Pure 3D Logo Presentation with Universal Celestial Orbit & Studio Shadow
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val orbitAngle = (orbitProgress * 2 * PI).toFloat()
                    val isFrontOrb = sin(orbitAngle) >= 0f
                    val tiltRad = (-15.0 * PI / 180.0).toFloat()
                    val cosTilt = cos(tiltRad)
                    val sinTilt = sin(tiltRad)
                    val rx = with(density) { 92.dp.toPx() }
                    val ry = with(density) { 36.dp.toPx() }

                    val localX = rx * cos(orbitAngle)
                    val localY = ry * sin(orbitAngle)
                    val orbRelX = localX * cosTilt - localY * sinTilt
                    val orbRelY = localX * sinTilt + localY * cosTilt

                    // Combined Universal System Container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 240.dp, height = 180.dp)
                            .graphicsLayer {
                                scaleX = entranceScale.value
                                scaleY = entranceScale.value
                                alpha = entranceAlpha.value
                            }
                    ) {
                        // Background Orbit Trajectory & back satellite orb (behind logo)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f

                            // Delicate celestial orbital ellipse (soft stardust dashes, low opacity)
                            rotate(degrees = -15f, pivot = Offset(cx, cy)) {
                                drawOval(
                                    color = Color(0xFF60A5FA).copy(alpha = 0.16f),
                                    topLeft = Offset(cx - rx, cy - ry),
                                    size = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f),
                                    style = Stroke(
                                        width = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
                                    )
                                )
                            }

                            // Satellite particle passing behind the 3D wallet
                            if (!isFrontOrb) {
                                val centerOrb = Offset(cx + orbRelX, cy + orbRelY)
                                drawCircle(
                                    color = Color(0xFF60A5FA).copy(alpha = 0.18f),
                                    radius = 5.dp.toPx(),
                                    center = centerOrb
                                )
                                drawCircle(
                                    color = Color(0xFFD8E2FE).copy(alpha = 0.38f),
                                    radius = 2.2.dp.toPx(),
                                    center = centerOrb
                                )
                            }
                        }

                        // Floating 3D Wallet Logo (Subtle studio motion, clean unclipped logo)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationX = entranceTiltX.value
                                    translationY = floatY * density.density
                                    rotationY = tiltY
                                    rotationZ = tiltZ
                                    cameraDistance = 14f * density.density
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icon_fg),
                                contentDescription = "My Wallet Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(150.dp)
                            )
                        }

                        // Foreground Orbit Satellite particle (passing in front of the 3D wallet)
                        if (isFrontOrb) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val centerOrb = Offset(cx + orbRelX, cy + orbRelY)

                                // Gentle stardust halo (soft, zero harsh glare)
                                drawCircle(
                                    color = Color(0xFF93C5FD).copy(alpha = 0.28f),
                                    radius = 6.dp.toPx(),
                                    center = centerOrb
                                )
                                // Soft celestial stardust core
                                drawCircle(
                                    color = Color(0xFFFFFFFF).copy(alpha = 0.60f),
                                    radius = 2.4.dp.toPx(),
                                    center = centerOrb
                                )
                            }
                        }
                    }

                    // Soft studio contact shadow beneath the 3D wallet logo
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(width = 110.dp, height = 12.dp)
                            .graphicsLayer {
                                scaleX = shadowScaleX * entranceAlpha.value
                                scaleY = entranceAlpha.value
                                alpha = shadowAlpha * entranceAlpha.value
                            }
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF000000).copy(alpha = 0.60f),
                                        Color(0xFF000000).copy(alpha = 0.20f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // App Name: ONLY "My Wallet" with smooth coordinated entrance
                    Text(
                        text = "My Wallet",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = textAlpha.value
                                translationY = textOffsetY.value * density.density
                            }
                    )
                }

                // Bottom: ONLY "Sign in with Google" Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            .height(54.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(27.dp),
                        color = Color.White,
                        shadowElevation = 5.dp,
                        border = BorderStroke(1.2.dp, Color(0xFFDADCE0)),
                        enabled = !isLoading
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color(0xFF3882F6),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Signing in...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1F1F1F)
                                    )
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1F1F1F),
                                        letterSpacing = 0.2.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Informative Dialog only appears if an authentication issue occurs
        if (errorDialogMessage != null) {
            AlertDialog(
                onDismissRequest = { errorDialogMessage = null },
                title = {
                    Text(
                        text = "Sign-In Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        SelectionContainer {
                            Text(
                                text = errorDialogMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { errorDialogMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3882F6))
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
                        Text("Copy Details", color = Color(0xFF93C5FD))
                    }
                },
                containerColor = Color(0xFF0D1B3E),
                tonalElevation = 6.dp
            )
        }
    }
}

internal fun getWebClientId(context: Context): String? {
    // 1. Dynamic reflection lookup on generated R.string class.
    try {
        val rClass = Class.forName("com.example.R\$string")
        val field = rClass.getField("default_web_client_id")
        val resId = field.getInt(null)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {
        // Fall through to package lookup
    }

    try {
        val rClass = Class.forName("${context.packageName}.R\$string")
        val field = rClass.getField("default_web_client_id")
        val resId = field.getInt(null)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {
        // Fall through to Resources.getIdentifier
    }

    // 2. Resource identifier lookup via application package name
    try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {}

    // 3. Resource identifier lookup via namespace "com.example"
    try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", "com.example")
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {}

    // 4. Resource identifier lookup via alternative resource name "web_client_id"
    try {
        val resId = context.resources.getIdentifier("web_client_id", "string", context.packageName)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
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
        onError("Google Sign-In configuration missing: Web OAuth Client ID (default_web_client_id) was not found in google-services resources. Please ensure Google Sign-In is enabled in Firebase Console and a valid google-services.json is present.")
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

    scope.launch {
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
                        Log.w("Auth", "GoogleIdTokenCredential.createFrom failed, reading bundle directly", e)
                        credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                            ?: credential.data.getString("id_token")
                    }
                }
                credential is CustomCredential -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: Exception) {
                        Log.w("Auth", "Fallback bundle lookup for custom credential: ${credential.type}", e)
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

            Log.d("Auth", "Google ID token obtained (token length: ${idToken.length}). Signing into Firebase...")
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

            val detailMsg = buildString {
                append("Sign-in bottom sheet was closed or dismissed ($rawMsg).\n\n")
                if (!matchesExpected) {
                    append("⚠️ SIGNING KEY MISMATCH DETECTED:\n")
                    append("This installed APK was signed with:\nSHA-1: $currentSha1\n\n")
                    append("Expected in google-services.json:\nSHA-1: ${AppSignatureHelper.EXPECTED_SHA1}\n\n")
                    append("Action Required: Please register this APK's SHA-1 ($currentSha1) in Firebase Console under Project Settings > Your apps.\n")
                } else {
                    append("Important: If the account picker closed immediately after you tapped your Gmail account, Google Play Services rejected the signing certificate (Status Code 10: Developer Error).\n\n")
                    append("How to resolve in Firebase Console:\n")
                    append("1. Open Firebase Console -> Project Settings -> General -> Your apps -> com.aistudio.mywallet.bdtnas\n")
                    append("2. Add SHA-1:\n   $currentSha1\n")
                    append("3. Add SHA-256:\n   $currentSha256\n")
                    append("4. In Authentication -> Sign-in method, ensure Google is Enabled.\n")
                }
            }
            onError(detailMsg)
        } catch (e: GetCredentialException) {
            Log.e("Auth", "GetCredentialException during Google Sign-In", e)
            val rawMsg = e.message ?: e.javaClass.simpleName
            val runtimeSig = AppSignatureHelper.getAppSignature(context)
            val currentSha1 = runtimeSig?.first ?: AppSignatureHelper.EXPECTED_SHA1
            val currentSha256 = runtimeSig?.second ?: AppSignatureHelper.EXPECTED_SHA256

            val friendlyMsg = when {
                rawMsg.contains("10") || rawMsg.contains("DeveloperError") ->
                    "Google Play Services Developer Error (10): APK signing certificate is not recognized in Firebase Console.\n\nAdd both to Firebase Console:\nSHA-1: $currentSha1\nSHA-256: $currentSha256"
                rawMsg.contains("16") || rawMsg.contains("Cannot find matching credential") ->
                    "Google Sign-In configuration error: Ensure Google provider is enabled in Firebase Authentication console."
                rawMsg.contains("7") || rawMsg.contains("NETWORK", ignoreCase = true) ->
                    "Network error: Unable to contact Google authentication servers. Please check your internet connection."
                rawMsg.contains("NoCredentialException") ->
                    "No Google account available or selected on this device."
                else ->
                    "Google Sign-In failed: $rawMsg"
            }
            onError(friendlyMsg)
        } catch (e: FirebaseNetworkException) {
            Log.e("Auth", "Firebase network error", e)
            onError("Network error: Could not reach Firebase servers. Please check your internet connection and try again.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("Auth", "Invalid Firebase credentials", e)
            onError("Authentication error: The Google ID token could not be verified by Firebase. Ensure Google Sign-In is enabled in Firebase Console.")
        } catch (e: FirebaseAuthException) {
            Log.e("Auth", "Firebase Auth error [${e.errorCode}]", e)
            val msg = e.localizedMessage ?: ""
            val isInternal = e.errorCode == "ERROR_INTERNAL_ERROR" || msg.contains("internal error", ignoreCase = true)
            val friendlyMsg = if (isInternal) {
                buildString {
                    append("⚠️ Firebase Authentication: Internal Error\n\n")
                    append("Root Cause:\n")
                    append("In Firebase project 'gen-lang-client-0425667793', Google Sign-In is either disabled or missing a Support Email in the Firebase Console.\n\n")
                    append("Easy 30-Second Fix in Firebase Console:\n")
                    append("1. Open: https://console.firebase.google.com/project/gen-lang-client-0425667793/authentication/providers\n")
                    append("2. Under 'Sign-in providers', click 'Google'.\n")
                    append("3. Turn the 'Enable' switch ON.\n")
                    append("4. Select your Project support email from the dropdown.\n")
                    append("5. Click 'Save'.\n\n")
                    append("Once saved, tap 'Sign in with Google' again!")
                }
            } else {
                "Firebase Authentication error (${e.errorCode}): ${e.localizedMessage ?: "Please try again"}"
            }
            onError(friendlyMsg)
        } catch (t: Throwable) {
            Log.e("Auth", "Unexpected error during Google Sign-In", t)
            val msg = t.localizedMessage ?: t.javaClass.simpleName
            val isInternal = msg.contains("internal error", ignoreCase = true)
            val friendlyMsg = if (isInternal) {
                buildString {
                    append("⚠️ Google Sign-In: Internal Error\n\n")
                    append("Google Sign-In provider must be enabled with a Support Email in Firebase Console (project: gen-lang-client-0425667793).\n\n")
                    append("Fix:\n")
                    append("1. Open Firebase Console -> Build -> Authentication -> Sign-in method\n")
                    append("2. Click 'Google' -> Enable -> Select support email -> Save\n")
                }
            } else {
                "Sign in failed: $msg"
            }
            onError(friendlyMsg)
        }
    }
}
