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
import androidx.compose.material.icons.filled.Person
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
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
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
 * Returns greeting string according to phase and selected user AppLanguage.
 */
fun getTimeGreeting(phase: TimePhase, language: AppLanguage): String {
    return when (language.langFamily) {
        "bn" -> phase.bengaliGreeting
        "ar" -> when (phase) {
            TimePhase.MORNING -> "صباح الخير"
            TimePhase.NOON, TimePhase.AFTERNOON -> "مساء الخير"
            TimePhase.EVENING -> "مساء النور"
            TimePhase.NIGHT -> "تصبح على خير"
        }
        "de" -> when (phase) {
            TimePhase.MORNING -> "Guten Morgen"
            TimePhase.NOON, TimePhase.AFTERNOON -> "Guten Tag"
            TimePhase.EVENING -> "Guten Abend"
            TimePhase.NIGHT -> "Gute Nacht"
        }
        "fr" -> when (phase) {
            TimePhase.MORNING -> "Bonjour"
            TimePhase.NOON, TimePhase.AFTERNOON -> "Bon après-midi"
            TimePhase.EVENING -> "Bonsoir"
            TimePhase.NIGHT -> "Bonne nuit"
        }
        "nl" -> when (phase) {
            TimePhase.MORNING -> "Goedemorgen"
            TimePhase.NOON, TimePhase.AFTERNOON -> "Goedemiddag"
            TimePhase.EVENING -> "Goedenavond"
            TimePhase.NIGHT -> "Goedenacht"
        }
        else -> phase.englishGreeting
    }
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
    val language = LocalAppLanguage.current
    val currentHour = rememberCurrentHour(fixedHour)
    val phase = remember(currentHour) { getTimePhase(currentHour) }
    val greetingText = remember(phase, language) { getTimeGreeting(phase, language) }
    val isDark = com.example.ui.theme.LocalAppTheme.current.isDark

    val userName = remember(userProfile?.displayName) {
        userProfile?.displayName?.ifBlank { "User" } ?: "User"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("time_greeting_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Avatar + Greeting + My Wallet Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Profile Avatar with Neon Glow Ring (Dark: Cyan, Lite: Blue)
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
                        width = 1.5.dp,
                        color = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.7f) else Color(0xFF38BDF8),
                        shape = CircleShape
                    )
                    .background(if (isDark) Color(0xFF0F1E40) else Color(0xFFDBEAFE))
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
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                        fontSize = 18.sp
                    )
                }
            }

            Column {
                // Greeting with Person icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$greetingText, $userName",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF080F20),
                        maxLines = 1,
                        modifier = Modifier.testTag("greeting_text")
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = appString(StringKeys.APP_NAME),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                    ),
                    modifier = Modifier.testTag("app_subtitle")
                )
            }
        }

        // Right side: Notification Icon and Settings Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notification icon button with unread badge dot
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF0C1A38) else Color(0xFFDBEAFE))
                    .border(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE), CircleShape)
                    .bounceClick(scaleDown = 0.9f) { onOpenNotifications() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                    modifier = Modifier.size(20.dp)
                )
                // Red badge dot at top-right
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
            }

            // Settings icon button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF0C1A38) else Color(0xFFDBEAFE))
                    .border(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE), CircleShape)
                    .bounceClick(scaleDown = 0.9f) { onOpenSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
