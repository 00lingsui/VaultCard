package com.example.cardmanager.core.database.entity

import androidx.compose.ui.graphics.Color

enum class CardStylePreset(
    val displayName: String,
    val primaryHex: String,
    val gradientHexes: List<String>,
    val isLightSurface: Boolean
) {
    PEARL_WHITE(
        displayName = "珍珠白",
        primaryHex = "#E2E8F0",
        gradientHexes = listOf("#F8FAFC", "#E2E8F0", "#CBD5E1"),
        isLightSurface = true
    ),
    GLACIER_MIST(
        displayName = "冰川雾蓝",
        primaryHex = "#DBEAFE",
        gradientHexes = listOf("#EFF6FF", "#DBEAFE", "#BFDBFE"),
        isLightSurface = true
    ),
    SAKURA_BLUSH(
        displayName = "樱花浅粉",
        primaryHex = "#FFE4E6",
        gradientHexes = listOf("#FFF1F2", "#FFE4E6", "#FECDD3"),
        isLightSurface = true
    ),
    MINT_BREEZE(
        displayName = "薄荷清风",
        primaryHex = "#D1FAE5",
        gradientHexes = listOf("#ECFDF5", "#D1FAE5", "#A7F3D0"),
        isLightSurface = true
    ),
    CHAMPAGNE_GOLD(
        displayName = "香槟流金",
        primaryHex = "#F1DFB6",
        gradientHexes = listOf("#FBF6E6", "#F1DFB6", "#DFC484"),
        isLightSurface = true
    ),
    AURORA_VIOLET(
        displayName = "极光幻紫",
        primaryHex = "#4F46E5",
        gradientHexes = listOf("#7C3AED", "#4F46E5", "#312E81"),
        isLightSurface = false
    ),
    DEEP_OCEAN(
        displayName = "深海静谧",
        primaryHex = "#1E3A8A",
        gradientHexes = listOf("#1E40AF", "#1E3A8A", "#0F172A"),
        isLightSurface = false
    ),
    OBSIDIAN_TITANIUM(
        displayName = "曜石黑钛",
        primaryHex = "#1E293B",
        gradientHexes = listOf("#334155", "#1E293B", "#090D16"),
        isLightSurface = false
    );

    val gradientColors: List<Color>
        get() = gradientHexes.map { Color(android.graphics.Color.parseColor(it)) }

    val primaryTextColor: Color
        get() = if (isLightSurface) Color(0xFF1E293B) else Color.White

    val secondaryTextColor: Color
        get() = if (isLightSurface) Color(0xFF475569) else Color.White.copy(alpha = 0.72f)

    val subtleSurfaceOverlay: Color
        get() = if (isLightSurface) Color(0xFF0F172A).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.18f)

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
