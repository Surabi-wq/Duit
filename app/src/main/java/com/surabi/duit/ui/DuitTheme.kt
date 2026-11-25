package com.surabi.duit.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DuitColors = lightColorScheme(
    primary = Color(0xFF147A52),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDF3E7),
    onPrimaryContainer = Color(0xFF073D29),
    secondary = Color(0xFF246FBA),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0EDFA),
    onSecondaryContainer = Color(0xFF173A5B),
    tertiary = Color(0xFFB66A17),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE9CE),
    onTertiaryContainer = Color(0xFF4B2B08),
    background = Color(0xFFF4F7F5),
    onBackground = Color(0xFF15251D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF15251D),
    surfaceVariant = Color(0xFFEAF0EC),
    onSurfaceVariant = Color(0xFF526158),
    outline = Color(0xFF7A8980),
    outlineVariant = Color(0xFFD6E0D9),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DuitShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun DuitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DuitColors,
        shapes = DuitShapes,
        content = content
    )
}
