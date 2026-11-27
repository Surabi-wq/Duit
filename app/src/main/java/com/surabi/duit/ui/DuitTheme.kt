package com.surabi.duit.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalDuitBalancesVisible = compositionLocalOf { true }

private val DuitLightColors = lightColorScheme(
    primary = Color(0xFF142B4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE1E9F3),
    onPrimaryContainer = Color(0xFF10243E),
    secondary = Color(0xFF9A7838),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3E9D4),
    onSecondaryContainer = Color(0xFF4D3915),
    tertiary = Color(0xFF426B78),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCECEF),
    onTertiaryContainer = Color(0xFF173B45),
    background = Color(0xFFF5F6F8),
    onBackground = Color(0xFF172333),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172333),
    surfaceVariant = Color(0xFFEBEEF2),
    onSurfaceVariant = Color(0xFF586575),
    outline = Color(0xFF8793A1),
    outlineVariant = Color(0xFFD9DEE5),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val DuitDarkColors = darkColorScheme(
    primary = Color(0xFFB9CBE4),
    onPrimary = Color(0xFF1D3554),
    primaryContainer = Color(0xFF304967),
    onPrimaryContainer = Color(0xFFE0EAF8),
    secondary = Color(0xFFE0C58D),
    onSecondary = Color(0xFF3C2D12),
    secondaryContainer = Color(0xFF594522),
    onSecondaryContainer = Color(0xFFF5E8C9),
    tertiary = Color(0xFFA9CDD5),
    onTertiary = Color(0xFF17363E),
    tertiaryContainer = Color(0xFF2C5059),
    onTertiaryContainer = Color(0xFFC5E8EF),
    background = Color(0xFF101720),
    onBackground = Color(0xFFE5EAF0),
    surface = Color(0xFF18222E),
    onSurface = Color(0xFFE5EAF0),
    surfaceVariant = Color(0xFF273341),
    onSurfaceVariant = Color(0xFFBAC5D2),
    outline = Color(0xFF8996A5),
    outlineVariant = Color(0xFF3D4A59),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

private val DuitShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

private val DuitTypography = Typography().copy(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun DuitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DuitDarkColors else DuitLightColors,
        typography = DuitTypography,
        shapes = DuitShapes,
        content = content
    )
}
