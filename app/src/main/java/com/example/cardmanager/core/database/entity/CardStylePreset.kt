package com.example.cardmanager.core.database.entity

import androidx.compose.ui.graphics.Color

enum class CardPatternStyle {
    GUILLOCHE_ARCS,
    TOPOGRAPHIC_WAVES,
    BRUSHED_RADIAL
}

enum class CardStylePreset(
    val displayName: String,
    val primaryHex: String,
    val gradientHexes: List<String>,
    val isLightSurface: Boolean,
    val accentBloomHex: String,
    val secondaryBloomHex: String,
    val useGoldEmvChip: Boolean,
    val patternStyle: CardPatternStyle
) {
    PEARL_WHITE(
        displayName = "珍珠白",
        primaryHex = "#E2E8F0",
        gradientHexes = listOf("#FFFFFF", "#F4F7FB", "#E4EBF3", "#CBD5E1"),
        isLightSurface = true,
        accentBloomHex = "#FFFFFF",
        secondaryBloomHex = "#94A3B8",
        useGoldEmvChip = false,
        patternStyle = CardPatternStyle.GUILLOCHE_ARCS
    ),
    GLACIER_MIST(
        displayName = "冰川雾蓝",
        primaryHex = "#DBEAFE",
        gradientHexes = listOf("#F5FAFF", "#E1EFFD", "#C7DFFD", "#A8CFFA"),
        isLightSurface = true,
        accentBloomHex = "#FFFFFF",
        secondaryBloomHex = "#60A5FA",
        useGoldEmvChip = false,
        patternStyle = CardPatternStyle.TOPOGRAPHIC_WAVES
    ),
    SAKURA_BLUSH(
        displayName = "樱花浅粉",
        primaryHex = "#FFE4E6",
        gradientHexes = listOf("#FFF5F7", "#FFE6EC", "#FFD0DC", "#FBB6C8"),
        isLightSurface = true,
        accentBloomHex = "#FFFFFF",
        secondaryBloomHex = "#F472B6",
        useGoldEmvChip = true,
        patternStyle = CardPatternStyle.GUILLOCHE_ARCS
    ),
    MINT_BREEZE(
        displayName = "薄荷清风",
        primaryHex = "#D1FAE5",
        gradientHexes = listOf("#F2FCF8", "#D7F7EA", "#B4EDD8", "#8DE0C4"),
        isLightSurface = true,
        accentBloomHex = "#FFFFFF",
        secondaryBloomHex = "#34D399",
        useGoldEmvChip = false,
        patternStyle = CardPatternStyle.TOPOGRAPHIC_WAVES
    ),
    CHAMPAGNE_GOLD(
        displayName = "香槟流金",
        primaryHex = "#F1DFB6",
        gradientHexes = listOf("#FDF9EE", "#F6E7C3", "#E8CD92", "#D5B165"),
        isLightSurface = true,
        accentBloomHex = "#FFFBEB",
        secondaryBloomHex = "#D97706",
        useGoldEmvChip = true,
        patternStyle = CardPatternStyle.BRUSHED_RADIAL
    ),
    AURORA_VIOLET(
        displayName = "极光幻紫",
        primaryHex = "#4F46E5",
        gradientHexes = listOf("#8B5CF6", "#6366F1", "#4338CA", "#1E1B4B"),
        isLightSurface = false,
        accentBloomHex = "#C4B5FD",
        secondaryBloomHex = "#38BDF8",
        useGoldEmvChip = false,
        patternStyle = CardPatternStyle.TOPOGRAPHIC_WAVES
    ),
    DEEP_OCEAN(
        displayName = "深海静谧",
        primaryHex = "#1E3A8A",
        gradientHexes = listOf("#2563EB", "#1D4ED8", "#172554", "#091128"),
        isLightSurface = false,
        accentBloomHex = "#60A5FA",
        secondaryBloomHex = "#06B6D4",
        useGoldEmvChip = false,
        patternStyle = CardPatternStyle.GUILLOCHE_ARCS
    ),
    OBSIDIAN_TITANIUM(
        displayName = "曜石黑钛",
        primaryHex = "#1E293B",
        gradientHexes = listOf("#475569", "#1E293B", "#0F172A", "#050811"),
        isLightSurface = false,
        accentBloomHex = "#94A3B8",
        secondaryBloomHex = "#F59E0B",
        useGoldEmvChip = true,
        patternStyle = CardPatternStyle.BRUSHED_RADIAL
    );

    val gradientColors: List<Color>
        get() = gradientHexes.map { Color(android.graphics.Color.parseColor(it)) }

    val primaryColor: Color
        get() = Color(android.graphics.Color.parseColor(primaryHex))

    val accentBloomColor: Color
        get() = Color(android.graphics.Color.parseColor(accentBloomHex))

    val secondaryBloomColor: Color
        get() = Color(android.graphics.Color.parseColor(secondaryBloomHex))

    val primaryTextColor: Color
        get() = if (isLightSurface) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    val secondaryTextColor: Color
        get() = if (isLightSurface) Color(0xFF475569) else Color.White.copy(alpha = 0.72f)

    val subtleSurfaceOverlay: Color
        get() = if (isLightSurface) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.18f)

    val glassPillBorderColor: Color
        get() = if (isLightSurface) Color(0xFF0F172A).copy(alpha = 0.14f) else Color.White.copy(alpha = 0.28f)

    val specularBorderColors: List<Color>
        get() = if (isLightSurface) {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.55f),
                Color(0xFF64748B).copy(alpha = 0.22f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.05f)
            )
        }

    val ambientShadowColor: Color
        get() = if (isLightSurface) {
            secondaryBloomColor.copy(alpha = 0.55f)
        } else {
            primaryColor.copy(alpha = 0.85f)
        }

    companion object {
        fun resolve(styleName: String?, fallbackHex: String?): CardStylePreset {
            if (!styleName.isNullOrBlank()) {
                entries.find { it.name.equals(styleName, ignoreCase = true) }?.let { return it }
            }
            if (!fallbackHex.isNullOrBlank()) {
                entries.find { it.primaryHex.equals(fallbackHex, ignoreCase = true) }?.let { return it }
                return when (fallbackHex.uppercase()) {
                    "#7C3AED" -> AURORA_VIOLET
                    "#B45309" -> CHAMPAGNE_GOLD
                    "#059669" -> MINT_BREEZE
                    "#0284C7" -> GLACIER_MIST
                    "#0F172A" -> OBSIDIAN_TITANIUM
                    else -> DEEP_OCEAN
                }
            }
            return DEEP_OCEAN
        }
    }
}
