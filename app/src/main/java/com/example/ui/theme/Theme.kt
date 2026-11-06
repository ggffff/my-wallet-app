package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/**
 * 5 Distinct Themes with unique color palettes, background depths, and glow accents:
 * 1. Cyber Neon: Deep Black, Electric Blue & Neon Pink
 * 2. Midnight Emerald: Obsidian, Jade & Mint Wealth
 * 3. Royal Amethyst: Velvet Void, Imperial Violet & Fuchsia
 * 4. Sunset Blaze: Espresso Charcoal, Flame Orange & Amber Gold
 * 5. Frosted Pearl: Crisp Light White, Sapphire & Rose
 */
enum class AppTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val isDark: Boolean,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val previewBg: Color
) {
    CYBER_NEON(
        id = "cyber_neon",
        displayName = "Dark Mode",
        subtitle = "Deep Navy (#080F20) • Blue Gradient Cards",
        isDark = true,
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF0070F3),
        glowColor = Color(0xFF38BDF8),
        previewBg = Color(0xFF080F20)
    ),
    MIDNIGHT_EMERALD(
        id = "midnight_emerald",
        displayName = "Midnight Emerald",
        subtitle = "Obsidian • Jade & Mint Wealth",
        isDark = true,
        primaryColor = EmeraldPrimary,
        secondaryColor = EmeraldSecondary,
        glowColor = EmeraldGlow,
        previewBg = EmeraldBackground
    ),
    ROYAL_AMETHYST(
        id = "royal_amethyst",
        displayName = "Royal Amethyst",
        subtitle = "Velvet Void • Imperial Violet",
        isDark = true,
        primaryColor = AmethystPrimary,
        secondaryColor = AmethystSecondary,
        glowColor = AmethystGlow,
        previewBg = AmethystBackground
    ),
    SUNSET_BLAZE(
        id = "sunset_blaze",
        displayName = "Sunset Blaze",
        subtitle = "Espresso • Flame & Amber Gold",
        isDark = true,
        primaryColor = SunsetPrimary,
        secondaryColor = SunsetSecondary,
        glowColor = SunsetGlow,
        previewBg = SunsetBackground
    ),
    FROSTED_PEARL(
        id = "frosted_pearl",
        displayName = "Lite Mode",
        subtitle = "Soft White (#F4F7FF) • Pure White & Soft Blue Cards",
        isDark = false,
        primaryColor = Color(0xFF0066FF),
        secondaryColor = Color(0xFF0284C7),
        glowColor = Color(0x330066FF),
        previewBg = Color(0xFFF4F7FF)
    );

    fun toColorScheme(): ColorScheme {
        return when (this) {
            CYBER_NEON -> CyberNeonColorScheme
            MIDNIGHT_EMERALD -> MidnightEmeraldColorScheme
            ROYAL_AMETHYST -> RoyalAmethystColorScheme
            SUNSET_BLAZE -> SunsetBlazeColorScheme
            FROSTED_PEARL -> FrostedPearlColorScheme
        }
    }

    companion object {
        fun fromId(id: String): AppTheme {
            return entries.find { it.id.equals(id, ignoreCase = true) }
                ?: if (id.equals("light", ignoreCase = true)) FROSTED_PEARL else CYBER_NEON
        }
    }
}

val LocalAppTheme = compositionLocalOf { AppTheme.CYBER_NEON }

// 1. Dark Mode Color Scheme (Deep Navy #080F20, Blue Gradient Cards, White Text)
val CyberNeonColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = PureWhite,
    primaryContainer = Color(0xFF0F1E40),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFF0070F3),
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF0B1733),
    onSecondaryContainer = Color(0xFF38BDF8),
    tertiary = IncomeGreen,
    onTertiary = PureWhite,
    error = ExpenseRed,
    onError = PureWhite,
    background = Color(0xFF080F20), // Deep Navy #080F20 as per specification
    onBackground = PureWhite,
    surface = Color(0xFF0B1733), // Dark Navy Card #0B1733
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF102244),
    onSurfaceVariant = Color(0xFF94A3B8), // Soft Grey
    outline = Color(0xFF1E3A8A), // Subtle Blue Border
    outlineVariant = Color(0xFF152A55)
)

// Backwards compatibility alias
val WalletDarkColorScheme = CyberNeonColorScheme

// 2. Midnight Emerald Color Scheme
val MidnightEmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhite,
    primaryContainer = EmeraldSurfaceElevated,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = EmeraldSecondary,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF0E3A2F),
    onSecondaryContainer = EmeraldGlow,
    tertiary = Color(0xFF34D399),
    onTertiary = PureWhite,
    error = ExpenseRed,
    onError = PureWhite,
    background = EmeraldBackground,
    onBackground = PureWhite,
    surface = EmeraldSurface,
    onSurface = PureWhite,
    surfaceVariant = EmeraldSurfaceElevated,
    onSurfaceVariant = Color(0xFF86EFAC),
    outline = EmeraldBorder
)

// 3. Royal Amethyst Color Scheme
val RoyalAmethystColorScheme = darkColorScheme(
    primary = AmethystPrimary,
    onPrimary = PureWhite,
    primaryContainer = AmethystSurfaceElevated,
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = AmethystSecondary,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF3B144E),
    onSecondaryContainer = Color(0xFFF0ABFC),
    tertiary = Color(0xFF38BDF8),
    onTertiary = PureWhite,
    error = ExpenseRed,
    onError = PureWhite,
    background = AmethystBackground,
    onBackground = PureWhite,
    surface = AmethystSurface,
    onSurface = PureWhite,
    surfaceVariant = AmethystSurfaceElevated,
    onSurfaceVariant = Color(0xFFC084FC),
    outline = AmethystBorder
)

// 4. Sunset Blaze Color Scheme
val SunsetBlazeColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = PureWhite,
    primaryContainer = SunsetSurfaceElevated,
    onPrimaryContainer = Color(0xFFFFEDD5),
    secondary = SunsetSecondary,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF432808),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFFE11D48),
    onTertiary = PureWhite,
    error = ExpenseRed,
    onError = PureWhite,
    background = SunsetBackground,
    onBackground = PureWhite,
    surface = SunsetSurface,
    onSurface = PureWhite,
    surfaceVariant = SunsetSurfaceElevated,
    onSurfaceVariant = Color(0xFFFDBA74),
    outline = SunsetBorder
)

// 5. Lite Mode Color Scheme (Soft White #F4F7FF, Pure White / Soft Blue Cards, Deep Navy Text)
val FrostedPearlColorScheme = lightColorScheme(
    primary = Color(0xFF0066FF),
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF0284C7),
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFF0F6FF),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = IncomeGreen,
    onTertiary = PureWhite,
    error = ExpenseRed,
    onError = PureWhite,
    background = Color(0xFFF4F7FF), // Soft White #F4F7FF as per specification
    onBackground = Color(0xFF080F20), // Deep Navy Text
    surface = Color(0xFFFFFFFF), // Pure White Cards
    onSurface = Color(0xFF080F20), // Deep Navy Text
    surfaceVariant = Color(0xFFF0F6FF), // Soft Blue
    onSurfaceVariant = Color(0xFF64748B), // Slate Grey Secondary Text
    outline = Color(0xFFCBD5E1), // Soft Blue Grey Border
    outlineVariant = Color(0xFFBFDBFE)
)

// Backwards compatibility alias
val WalletLightColorScheme = FrostedPearlColorScheme

@Composable
fun animatedColorScheme(
    targetColorScheme: ColorScheme,
    durationMs: Int = 420
): ColorScheme {
    val animSpec = tween<Color>(durationMillis = durationMs, easing = FastOutSlowInEasing)

    val primary by animateColorAsState(targetColorScheme.primary, animSpec, label = "primary")
    val onPrimary by animateColorAsState(targetColorScheme.onPrimary, animSpec, label = "onPrimary")
    val primaryContainer by animateColorAsState(targetColorScheme.primaryContainer, animSpec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, animSpec, label = "onPrimaryContainer")
    val secondary by animateColorAsState(targetColorScheme.secondary, animSpec, label = "secondary")
    val onSecondary by animateColorAsState(targetColorScheme.onSecondary, animSpec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, animSpec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetColorScheme.onSecondaryContainer, animSpec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(targetColorScheme.tertiary, animSpec, label = "tertiary")
    val onTertiary by animateColorAsState(targetColorScheme.onTertiary, animSpec, label = "onTertiary")
    val error by animateColorAsState(targetColorScheme.error, animSpec, label = "error")
    val onError by animateColorAsState(targetColorScheme.onError, animSpec, label = "onError")
    val background by animateColorAsState(targetColorScheme.background, animSpec, label = "background")
    val onBackground by animateColorAsState(targetColorScheme.onBackground, animSpec, label = "onBackground")
    val surface by animateColorAsState(targetColorScheme.surface, animSpec, label = "surface")
    val onSurface by animateColorAsState(targetColorScheme.onSurface, animSpec, label = "onSurface")
    val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, animSpec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, animSpec, label = "onSurfaceVariant")
    val outline by animateColorAsState(targetColorScheme.outline, animSpec, label = "outline")

    return targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        error = error,
        onError = onError,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline
    )
}

@Composable
fun MyWalletTheme(
    appTheme: AppTheme = AppTheme.CYBER_NEON,
    darkTheme: Boolean = appTheme.isDark,
    content: @Composable () -> Unit
) {
    // If darkTheme boolean is passed explicitly without appTheme override, map properly
    val resolvedTheme = if (appTheme == AppTheme.CYBER_NEON && !darkTheme) {
        AppTheme.FROSTED_PEARL
    } else {
        appTheme
    }

    val baseScheme = resolvedTheme.toColorScheme()
    val animatedScheme = animatedColorScheme(baseScheme)

    CompositionLocalProvider(LocalAppTheme provides resolvedTheme) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = Typography,
            content = content
        )
    }
}
