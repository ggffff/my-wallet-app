package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WalletDarkColorScheme = darkColorScheme(
    primary = BrandGlow,
    onPrimary = Color(0xFF010E27),
    primaryContainer = Color(0xFF0A3670),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = BrandPrimaryLight,
    onSecondary = Color(0xFF010E27),
    secondaryContainer = Color(0xFF132B4F),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = IncomeGreen,
    onTertiary = Color.White,
    error = ExpenseRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

val WalletLightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = InfoSky,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = IncomeGreen,
    onTertiary = Color.White,
    error = ExpenseRed,
    onError = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun MyWalletTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WalletDarkColorScheme else WalletLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
