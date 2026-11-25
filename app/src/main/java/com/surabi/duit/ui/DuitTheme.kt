package com.surabi.duit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DuitColors = lightColorScheme(
    primary = Color(0xFF16875D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDF2E7),
    onPrimaryContainer = Color(0xFF073D29),
    secondary = Color(0xFF2475C7),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCEBFA),
    onSecondaryContainer = Color(0xFF102E4E),
    background = Color(0xFFF5F8F6),
    onBackground = Color(0xFF17231D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17231D),
    surfaceVariant = Color(0xFFEAF0EC),
    onSurfaceVariant = Color(0xFF526158),
    error = Color(0xFFBA1A1A)
)

@Composable
fun DuitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DuitColors,
        content = content
    )
}
