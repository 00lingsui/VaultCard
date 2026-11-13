package com.example.cardmanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
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

enum class AppThemePalette(
    val displayNameZh: String,
    val displayNameEn: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val lightContainerColor: Color,
    val darkContainerColor: Color
) {
    OCEAN_BLUE(
        displayNameZh = "经典海蓝",
        displayNameEn = "Ocean Blue",
        primaryColor = Color(0xFF007DFF),
        secondaryColor = Color(0xFF0EA5E9),
        lightContainerColor = Color(0xFFE0F2FE),
        darkContainerColor = Color(0xFF0C4A6E)
    ),
    EMERALD_FOREST(
        displayNameZh = "翡翠森林",
        displayNameEn = "Emerald Forest",
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF10B981),
        lightContainerColor = Color(0xFFD1FAE5),
        darkContainerColor = Color(0xFF064E3B)
    ),
    TWILIGHT_PURPLE(
        displayNameZh = "暮光紫韵",
        displayNameEn = "Twilight Purple",
        primaryColor = Color(0xFF7C3AED),
        secondaryColor = Color(0xFFA855F7),
        lightContainerColor = Color(0xFFEDE9FE),
        darkContainerColor = Color(0xFF4C1D95)
    ),
    SUNSET_CORAL(
        displayNameZh = "日落珊瑚",
        displayNameEn = "Sunset Coral",
        primaryColor = Color(0xFFEA580C),
        secondaryColor = Color(0xFFF97316),
        lightContainerColor = Color(0xFFFFEDD5),
        darkContainerColor = Color(0xFF7C2D12)
    ),
    OBSIDIAN_GOLD(
        displayNameZh = "黑金尊享",
        displayNameEn = "Obsidian Gold",
        primaryColor = Color(0xFFD97706),
        secondaryColor = Color(0xFFF59E0B),
        lightContainerColor = Color(0xFFFEF3C7),
        darkContainerColor = Color(0xFF78350F)
    ),
    ROSE_QUARTZ(
        displayNameZh = "蔷薇粉晶",
        displayNameEn = "Rose Quartz",
        primaryColor = Color(0xFFE11D48),
        secondaryColor = Color(0xFFF43F5E),
        lightContainerColor = Color(0xFFFFE4E6),
        darkContainerColor = Color(0xFF881337)
    )
}

val MiuiSurface = Color(0xFFF7F9FC)
val MiuiSurfaceDark = Color(0xFF14171D)
val MiuiCardLight = Color(0xFFFFFFFF)
val MiuiCardDark = Color(0xFF1E222B)
val MiuiTextPrimary = Color(0xFF0F172A)
val MiuiTextSecondary = Color(0xFF64748B)

fun buildMiuiColorScheme(darkTheme: Boolean, palette: AppThemePalette): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = palette.secondaryColor,
            onPrimary = Color.White,
            primaryContainer = palette.darkContainerColor,
            onPrimaryContainer = Color.White,
            secondary = palette.primaryColor,
            background = MiuiSurfaceDark,
            surface = MiuiCardDark,
            surfaceVariant = Color(0xFF262C36),
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFF94A3B8)
        )
    } else {
        lightColorScheme(
            primary = palette.primaryColor,
            onPrimary = Color.White,
            primaryContainer = palette.lightContainerColor,
            onPrimaryContainer = palette.darkContainerColor,
            secondary = palette.secondaryColor,
            background = MiuiSurface,
            surface = MiuiCardLight,
            surfaceVariant = Color(0xFFEDF2F7),
            onSurface = MiuiTextPrimary,
            onSurfaceVariant = MiuiTextSecondary
        )
    }
}

fun buildMaterial3ColorScheme(darkTheme: Boolean, palette: AppThemePalette): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = palette.secondaryColor,
            onPrimary = Color.White,
            primaryContainer = palette.darkContainerColor,
            onPrimaryContainer = palette.lightContainerColor,
            secondary = palette.primaryColor,
            secondaryContainer = palette.darkContainerColor.copy(alpha = 0.7f),
            background = Color(0xFF111318),
            surface = Color(0xFF191C22),
            surfaceVariant = Color(0xFF2B2F3A)
        )
    } else {
        lightColorScheme(
            primary = palette.primaryColor,
            onPrimary = Color.White,
            primaryContainer = palette.lightContainerColor,
            onPrimaryContainer = palette.darkContainerColor,
            secondary = palette.secondaryColor,
            secondaryContainer = palette.lightContainerColor.copy(alpha = 0.75f),
            background = Color(0xFFFAF9FD),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE7E9F2)
        )
    }
}

val MiuiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val Md3Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
    palette: AppThemePalette = AppThemePalette.OCEAN_BLUE,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = buildMiuiColorScheme(darkTheme = darkTheme, palette = palette),
        shapes = MiuiShapes,
        typography = MiuiTypography,
        content = content
    )
}
