package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SolarPumpColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0x3306B6D4),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = EmeraldStart,
    onSecondary = Color.Black,
    secondaryContainer = Color(0x3310B981),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = BgSlateNavy,
    onBackground = TextPrimary,
    surface = GlassCardBg,
    onSurface = TextPrimary,
    surfaceVariant = Color(0x331E293B),
    onSurfaceVariant = TextSecondary,
    error = RoseStop,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SolarPumpColorScheme,
        typography = AppTypography,
        content = content
    )
}
