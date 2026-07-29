package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBg,
    onPrimaryContainer = PrimaryDarkBlue,
    secondary = AccentSky,
    onSecondary = Color.White,
    tertiary = DairyGold,
    background = BgLight,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = CardBorder,
    error = AlarmRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLightBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryDarkBlue,
    onPrimaryContainer = AccentBg,
    secondary = AccentSky,
    onSecondary = Color.Black,
    tertiary = DairyGold,
    background = PrimaryDarkBlue,
    onBackground = Color.White,
    surface = Color(0xFF152A4A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF203C66),
    error = AlarmRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
