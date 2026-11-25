package com.surabi.duit.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DuitColors = lightColorScheme(
    primary = Color(0xFF087C55),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDF8EA),
    onPrimaryContainer = Color(0xFF064A32),
    secondary = Color(0xFF2563C7),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2EDFF),
    onSecondaryContainer = Color(0xFF153A70),
    tertiary = Color(0xFFE28B21),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFECCC),
    onTertiaryContainer = Color(0xFF59330A),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF15251F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF15251F),
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = Color(0xFF526274),
    outline = Color(0xFF8997A8),
    outlineVariant = Color(0xFFDCE4ED),
    error = Color(0xFFD54448),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFCE5E6),
    onErrorContainer = Color(0xFF5F1118)
)

private val DuitShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

private val DuitTypography = Typography().copy(
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun DuitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DuitColors,
        typography = DuitTypography,
        shapes = DuitShapes,
        content = content
    )
}
