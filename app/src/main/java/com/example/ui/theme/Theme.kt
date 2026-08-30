package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JadePrimary,
    onPrimary = Color(0xFF00381B),
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = Color(0xFF70FCA8),
    secondary = GoldAmber,
    onSecondary = Color(0xFF412D00),
    secondaryContainer = Color(0xFF5E4200),
    onSecondaryContainer = Color(0xFFFFDEA0),
    tertiary = CosmicPurple,
    onTertiary = Color.White,
    background = SectDarkBackground,
    onBackground = CultivationTextPrimary,
    surface = SectDarkSurface,
    onSurface = CultivationTextPrimary,
    surfaceVariant = SectDarkSurfaceVariant,
    onSurfaceVariant = CultivationTextSecondary,
    error = CinnabarRed
)

private val LightColorScheme = darkColorScheme( // Cultivation games shine best in dark mystical aesthetic
    primary = JadeDark,
    onPrimary = Color.White,
    secondary = GoldAmber,
    tertiary = DaoistViolet,
    background = Color(0xFF10121D),
    surface = Color(0xFF1A1C2C),
    onBackground = CultivationTextPrimary,
    onSurface = CultivationTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep immersive martial aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
