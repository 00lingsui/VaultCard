package com.example.cardmanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MiuiPrimary = Color(0xFF007DFF)
val MiuiSurface = Color(0xFFF7F9FC)
val MiuiSurfaceDark = Color(0xFF14171D)
val MiuiCardLight = Color(0xFFFFFFFF)
val MiuiCardDark = Color(0xFF1E222B)
val MiuiTextPrimary = Color(0xFF0F172A)
val MiuiTextSecondary = Color(0xFF64748B)

private val MiuiLightColorScheme = lightColorScheme(
    primary = MiuiPrimary,
    onPrimary = Color.White,
    background = MiuiSurface,
    surface = MiuiCardLight,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurface = MiuiTextPrimary,
    onSurfaceVariant = MiuiTextSecondary
)

private val MiuiDarkColorScheme = darkColorScheme(
    primary = MiuiPrimary,
    onPrimary = Color.White,
    background = MiuiSurfaceDark,
    surface = MiuiCardDark,
    surfaceVariant = Color(0xFF262C36),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF94A3B8)
)

val MiuiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val MiuiTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MiuiTextSecondary)
)

@Composable
fun MIUIXTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MiuiDarkColorScheme else MiuiLightColorScheme,
        shapes = MiuiShapes,
        typography = MiuiTypography,
        content = content
    )
}
