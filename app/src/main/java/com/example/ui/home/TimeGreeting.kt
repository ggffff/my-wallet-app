package com.example.ui.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.motion.bounceClick
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

/**
 * 5 distinct time-based phases with Bengali & English greetings
 * as requested:
 *
 * BENGALI:
 * Morning: শুভ সকাল
 * Noon: শুভ দুপুর
 * Afternoon: শুভ বিকেল
 * Evening: শুভ সন্ধ্যা
 * Night: শুভ রাত্রি
 *
 * ENGLISH:
 * Morning: Good Morning
 * Noon: Good Afternoon
 * Afternoon: Good Afternoon
 * Evening: Good Evening
 * Night: Good Night
 */
enum class TimePhase(
    val englishGreeting: String,
    val bengaliGreeting: String,
    val icon: ImageVector,
    val lightIconColor: Color,
    val darkIconColor: Color,
    val lightBadgeBg: Color,
    val darkBadgeBg: Color,
    val phaseName: String
) {
    MORNING(
        englishGreeting = "Good Morning",
        bengaliGreeting = "শুভ সকাল",
        icon = Icons.Filled.LightMode,
        lightIconColor = Color(0xFFD97706),
        darkIconColor = Color(0xFFFBBF24),
        lightBadgeBg = Color(0xFFFEF3C7),
        darkBadgeBg = Color(0x33F59E0B),
        phaseName = "Morning"
    ),
    NOON(
        englishGreeting = "Good Afternoon",
        bengaliGreeting = "শুভ দুপুর",
        icon = Icons.Filled.WbSunny,
        lightIconColor = Color(0xFFEAB308),
        darkIconColor = Color(0xFFFDE047),
        lightBadgeBg = Color(0xFFFEF9C3),
        darkBadgeBg = Color(0x33EAB308),
        phaseName = "Noon"
    ),
    AFTERNOON(
        englishGreeting = "Good Afternoon",
        bengaliGreeting = "শুভ বিকেল",
        icon = Icons.Filled.WbSunny,
        lightIconColor = Color(0xFFEA580C),
        darkIconColor = Color(0xFFFB923C),
        lightBadgeBg = Color(0xFFFFEDD5),
        darkBadgeBg = Color(0x33F97316),
        phaseName = "Afternoon"
    ),
    EVENING(
        englishGreeting = "Good Evening",
        bengaliGreeting = "শুভ সন্ধ্যা",
        icon = Icons.Filled.WbTwilight,
        lightIconColor = Color(0xFFE11D48),
        darkIconColor = Color(0xFFFB7185),
        lightBadgeBg = Color(0xFFFFE4E6),
        darkBadgeBg = Color(0x33F43F5E),
        phaseName = "Evening"
    ),
    NIGHT(
        englishGreeting = "Good Night",
        bengaliGreeting = "শুভ রাত্রি",
        icon = Icons.Filled.NightsStay,
        lightIconColor = Color(0xFF4F46E5),
        darkIconColor = Color(0xFFA5B4FC),
        lightBadgeBg = Color(0xFFE0E7FF),
        darkBadgeBg = Color(0x336366F1),
        phaseName = "Night"
    )
}

/**
 * Maps hour of day (0..23) to corresponding TimePhase.
 * 5:00 - 11:59 -> MORNING
 * 12:00 - 14:59 -> NOON
 * 15:00 - 17:59 -> AFTERNOON
 * 18:00 - 21:59 -> EVENING
 * 22:00 - 04:59 -> NIGHT
 */
fun getTimePhase(hour: Int): TimePhase {
    val h = ((hour % 24) + 24) % 24
    return when (h) {
        in 5..11 -> TimePhase.MORNING
        in 12..14 -> TimePhase.NOON
        in 15..17 -> TimePhase.AFTERNOON
        in 18..21 -> TimePhase.EVENING
        else -> TimePhase.NIGHT
    }
}

/**
 * Returns greeting string according to phase and system language.
 */
fun getTimeGreeting(phase: TimePhase, isBengali: Boolean): String {
    return if (isBengali) phase.bengaliGreeting else phase.englishGreeting
}

/**
 * Convenience helper to get greeting from hour directly.
 */
fun getTimeGreetingForHour(hour: Int, isBengali: Boolean): String {
    return getTimeGreeting(getTimePhase(hour), isBengali)
}

/**
 * Detects whether the system language is Bengali (bn).
 * Uses Android ConfigurationCompat to inspect system locales,
 * falling back to Locale.getDefault() if necessary.
 */
fun isSystemBengali(configuration: Configuration?): Boolean {
    val primaryLocale = if (configuration != null) {
        val locales = ConfigurationCompat.getLocales(configuration)
        if (!locales.isEmpty) locales.get(0) else Locale.getDefault()
    } else {
        Locale.getDefault()
    }
    val lang = (primaryLocale?.language ?: "").lowercase(Locale.ROOT)
    return lang.startsWith("bn")
}

fun isSystemBengali(locale: Locale?): Boolean {
    val lang = (locale?.language ?: "").lowercase(Locale.ROOT)
    return lang.startsWith("bn")
}

/**
 * Reactively observes Android system configuration language in Compose.
 * Will recompose automatically whenever the user changes system language.
 */
@Composable
fun rememberSystemIsBengali(): Boolean {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        isSystemBengali(configuration)
    }
}

/**
 * Keeps track of current hour and updates automatically every 15 seconds.
 */
@Composable
fun rememberCurrentHour(fixedHour: Int? = null): Int {
    if (fixedHour != null) return fixedHour
    var hour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000L)
            val current = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (current != hour) {
                hour = current
            }
        }
    }
    return hour
}

/**
 * Full top-bar header with matching Sun/Moon animated icon,
 * smooth fade greeting transition, user profile image with neon ring,
 * Notification and Settings action icons, and app title.
 */
@Composable
fun TimeBasedGreetingHeader(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    fixedHour: Int? = null,
    unreadNotificationsCount: Int = 0,
    onOpenNotifications: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val isBengali = rememberSystemIsBengali()
    val currentHour = rememberCurrentHour(fixedHour)
    val phase = remember(currentHour) { getTimePhase(currentHour) }
    val greetingText = remember(phase, isBengali) { getTimeGreeting(phase, isBengali) }
    val isDark = isSystemInDarkTheme()

    val userName = remember(userProfile?.displayName) {
        userProfile?.displayName?.ifBlank { "NU Sifat" } ?: "NU Sifat"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("time_greeting_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Avatar + Greeting + Sun/Moon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Profile Avatar with Neon Glow Ring
            val photoUrl = userProfile?.photoUrl.orEmpty()
            val isCustomPhoto = photoUrl.startsWith("file:") ||
                    photoUrl.startsWith("content:") ||
                    photoUrl.startsWith("http:") ||
                    photoUrl.startsWith("https:") ||
                    photoUrl.contains("/")

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF00F0FF),
                                Color(0xFF0070F3),
                                Color(0xFF00E5FF)
                            )
                        ),
                        shape = CircleShape
                    )
                    .background(Color(0xFF0F1E3A))
                    .bounceClick { onOpenSettings() }
                    .testTag("profile_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isCustomPhoto) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val initialLetter = (userProfile?.displayName?.take(1)?.uppercase()?.ifBlank { "N" } ?: "N")
                    Text(
                        text = initialLetter,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00F0FF),
                        fontSize = 18.sp
                    )
                }
            }

            Column {
                // Greeting and Sun/Moon indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Matching Sun / Moon Animated Icon with smooth crossfade
                    AnimatedContent(
                        targetState = phase,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(400, easing = LinearOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.82f, animationSpec = tween(400))) togetherWith
                                    (fadeOut(animationSpec = tween(350, easing = FastOutLinearInEasing)) +
                                            scaleOut(targetScale = 1.08f, animationSpec = tween(350)))
                        },
                        label = "greetingIconCrossfade"
                    ) { targetPhase ->
                        val iconColor = if (isDark) targetPhase.darkIconColor else targetPhase.lightIconColor

                        Icon(
                            imageVector = targetPhase.icon,
                            contentDescription = targetPhase.phaseName,
                            tint = iconColor,
                            modifier = Modifier
                                .size(16.dp)
                                .testTag("greeting_icon_${targetPhase.name.lowercase()}")
                        )
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    // Smooth fade animated greeting text
                    AnimatedContent(
                        targetState = greetingText,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(450, easing = LinearOutSlowInEasing)) togetherWith
                                    fadeOut(animationSpec = tween(450, easing = FastOutLinearInEasing))
                        },
                        label = "greetingTextFade"
                    ) { text ->
                        Text(
                            text = "$text, $userName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            modifier = Modifier.testTag("greeting_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "My Wallet",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00F0FF)
                    ),
                    modifier = Modifier.testTag("app_subtitle")
                )
            }
        }

        // Right side: Notification Icon and Settings Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Notification icon button with neon glass style and unread badge dot
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x2600F0FF))
                    .border(1.dp, Color(0x4000F0FF), CircleShape)
                    .bounceClick(scaleDown = 0.9f) { onOpenNotifications() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(20.dp)
                )
                if (unreadNotificationsCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                }
            }

            // Settings icon button with neon glass style
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x2600F0FF))
                    .border(1.dp, Color(0x4000F0FF), CircleShape)
                    .bounceClick(scaleDown = 0.9f) { onOpenSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
