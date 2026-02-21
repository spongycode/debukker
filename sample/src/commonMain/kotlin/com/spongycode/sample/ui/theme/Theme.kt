package com.spongycode.sample.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CinematicDark = darkColorScheme(
    primary = Color(0xFFFFC107), // Amber/Gold
    onPrimary = Color(0xFF1C1B1F),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF1C1B1F),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
)

@Composable
fun RecipeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CinematicDark,
        typography = Typography(),
        content = content
    )
}
