package com.example.cardmanager.feature.card.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.cardmanager.ui.icons.AppIcons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.R
import com.example.cardmanager.core.common.util.calculateDaysUntilRepayment
import com.example.cardmanager.core.currency.AppCurrency
import com.example.cardmanager.core.database.entity.CardNetwork
import com.example.cardmanager.core.database.entity.CardPatternStyle
import com.example.cardmanager.core.database.entity.CardStylePreset
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.feedback.FeedbackManager
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState

private const val PHYSICAL_CARD_ASPECT_RATIO = 1.586f

@Composable
fun CreditCardItem(
    card: CreditCardEntity,
    modifier: Modifier = Modifier,
    recordedSwipesInCurrentYear: Int = 0,
    cardDebt: Double = 0.0,
    initiallyExpanded: Boolean = false,
    onClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null
) {
    val themeState = LocalThemeState.current
    val view = LocalView.current
    val isMiui = themeState.currentStyle == AppThemeStyle.MIUIX
    val cardShape = remember { RoundedCornerShape(20.dp) }

    var expanded by remember { mutableStateOf(initiallyExpanded) }

    val daysRemaining = card.calculateDaysUntilRepayment()
    val cardCurrency = remember(card.currencyCode) { AppCurrency.fromCode(card.currencyCode) }
    val availableCredit = (card.creditLimit - cardDebt).coerceAtLeast(0.0)
    val stylePreset = remember(card.cardStyle, card.cardColorHex) {
        CardStylePreset.resolve(card.cardStyle, card.cardColorHex)
    }
    val isLightSurface = stylePreset.isLightSurface
    val primaryText = stylePreset.primaryTextColor
    val secondaryText = stylePreset.secondaryTextColor

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.975f
            expanded -> 1.008f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "cardScale"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 3.dp
            expanded -> 10.dp
            else -> 6.dp
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
        label = "cardElevation"
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "arrowRotation"
    )

    val specularBorder = remember(stylePreset) {
        BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = stylePreset.specularBorderColors,
                start = Offset(0f, 0f),
                end = Offset(900f, 600f)
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = cardShape,
        color = stylePreset.gradientColors.last(),
        shadowElevation = elevation,
        border = specularBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        FeedbackManager.performClick(view)
                        expanded = !expanded
                    }
                )
        ) {
            LuxuryCardSurfaceLayer(
                stylePreset = stylePreset,
                modifier = Modifier.matchParentSize()
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                // Physical Bank Card Face (~1.586:1 ISO/IEC 7810 ID-1 Aspect Ratio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(PHYSICAL_CARD_ASPECT_RATIO)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Row: Bank Name, Frosted Waiver Pill, Due Badge & NFC Contactless Symbol
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = card.bankName.uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            letterSpacing = 0.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = primaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if (card.hasAnnualFee && card.annualFeeWaiverCount > 0) {
                                        val completedSwipes = card.initialWaiverSwipes + recordedSwipesInCurrentYear
                                        val remainingSwipes = maxOf(0, card.annualFeeWaiverCount - completedSwipes)
                                        val isWaived = remainingSwipes == 0

                                        FrostedWaiverPill(
                                            text = if (isWaived) {
                                                stringResource(R.string.waiver_badge_completed)
                                            } else {
                                                stringResource(R.string.waiver_badge_remaining, remainingSwipes)
                                            },
                                            isWaived = isWaived,
                                            isLightSurface = isLightSurface
                                        )
                                    }
                                }

                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = card.cardAlias,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = if (isMiui) (-0.3).sp else 0.sp
                                    ),
                                    color = secondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val (badgeBg, dotColor, badgeText) = when {
                                    daysRemaining <= 0L -> Triple(Color(0xFFDC2626), Color(0xFFFF8A80), "今日到期")
                                    daysRemaining <= 3L -> Triple(Color(0xFFEF4444), Color(0xFFFFB4AB), "还剩 ${daysRemaining} 天")
                                    daysRemaining <= 7L -> Triple(Color(0xFFD97706), Color(0xFFFFD54F), "还剩 ${daysRemaining} 天")
                                    else -> Triple(Color(0xFF059669), Color(0xFFA7F3D0), "还剩 ${daysRemaining} 天")
                                }

                                Surface(
                                    color = badgeBg.copy(alpha = 0.88f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.5.dp)
                                                .background(dotColor, CircleShape)
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            text = badgeText,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            )
                                        )
                                    }
                                }

                                ContactlessWavesGraphic(waveColor = secondaryText)

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "展开卡片",
                                    tint = secondaryText,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(arrowRotation)
                                )
                            }
                        }

                        // Middle Row: Realistic Metallic EMV Smart Chip & Tabular Masked Card Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EmvChipGraphic(useGoldAlloy = stylePreset.useGoldEmvChip)

                            Text(
                                text = "••••  ••••  ••••  ${card.last4Digits}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontFeatureSettings = "tnum",
                                    letterSpacing = 1.8.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = primaryText,
                                maxLines = 1
                            )
                        }

                        // Bottom Row: Available Credit Micro-Label + Tabular Amount & CardNetworkBadge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                Text(
                                    text = stringResource(R.string.card_available_micro_label),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 0.9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = primaryText.copy(alpha = 0.70f)
                                )
                                Spacer(Modifier.height(1.dp))
                                Text(
                                    text = cardCurrency.format(availableCredit),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum",
                                        letterSpacing = (-0.4).sp
                                    ),
                                    color = primaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "额度 ${cardCurrency.format(card.creditLimit, trimWholeDecimals = true)} · 每月 ${card.repaymentDay} 日还",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = secondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            CardNetworkBadge(
                                network = card.cardNetwork,
                                isLightSurface = isLightSurface
                            )
                        }
                    }
                }

                // Spring-Animated Expanded Details Section (single unified container extension)
                val drawerAnimSpec = spring<androidx.compose.ui.unit.IntSize>(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
                val fadeAnimSpec = spring<Float>(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(animationSpec = drawerAnimSpec) + fadeIn(animationSpec = fadeAnimSpec),
                    exit = shrinkVertically(animationSpec = drawerAnimSpec) + fadeOut(animationSpec = fadeAnimSpec)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isLightSurface) {
                                    Color.White.copy(alpha = 0.24f)
                                } else {
                                    Color.Black.copy(alpha = 0.16f)
                                }
                            )
                            .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
                    ) {
                        HorizontalDivider(
                            color = if (isLightSurface) Color(0xFF0F172A).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.16f),
                            thickness = 0.75.dp
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "账单日",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                    color = secondaryText
                                )
                                Text(
                                    text = "每月 ${card.billDay} 日",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = primaryText
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "卡组织网络",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                    color = secondaryText
                                )
                                Text(
                                    text = card.cardNetwork.localizedName(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = primaryText
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        val formattedFee = cardCurrency.format(card.annualFeeAmount, trimWholeDecimals = true)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.annual_fee_policy_label),
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                    color = secondaryText
                                )
                                val feePolicyText = if (card.hasAnnualFee) {
                                    formattedFee
                                } else {
                                    stringResource(R.string.annual_fee_free_badge)
                                }
                                val feeColor = when {
                                    card.hasAnnualFee && isLightSurface -> Color(0xFFB45309)
                                    card.hasAnnualFee -> Color(0xFFFBBF24)
                                    isLightSurface -> Color(0xFF047857)
                                    else -> Color(0xFF6EE7B7)
                                }
                                Text(
                                    text = feePolicyText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = feeColor
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1.4f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = stringResource(R.string.annual_fee_waiver_req_label),
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                    color = secondaryText
                                )
                                val waiverDetailText = when {
                                    !card.hasAnnualFee -> stringResource(R.string.annual_fee_free_desc)
                                    card.annualFeeWaiverCount > 0 -> stringResource(R.string.annual_fee_waivable_summary, formattedFee, card.annualFeeWaiverCount)
                                    else -> stringResource(R.string.annual_fee_rigid_summary, formattedFee)
                                }
                                Text(
                                    text = waiverDetailText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = primaryText
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    FeedbackManager.performClick(view)
                                    onClick()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = stylePreset.subtleSurfaceOverlay,
                                    contentColor = primaryText
                                )
                            ) {
                                Icon(
                                    imageVector = AppIcons.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "管理卡片",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (onDeleteClick != null) {
                                IconButton(
                                    onClick = {
                                        FeedbackManager.performDestructive(view)
                                        onDeleteClick()
                                    },
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFFDC2626).copy(alpha = if (isLightSurface) 0.14f else 0.28f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除卡片",
                                        tint = if (isLightSurface) Color(0xFFDC2626) else Color(0xFFFF6B6B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardPreviewBanner(
    bankName: String,
    cardAlias: String,
    last4Digits: String,
    creditLimit: Double,
    repaymentDay: Int,
    stylePreset: CardStylePreset,
    network: CardNetwork,
    modifier: Modifier = Modifier,
    currency: AppCurrency = AppCurrency.CNY
) {
    val isLightSurface = stylePreset.isLightSurface
    val primaryText = stylePreset.primaryTextColor
    val secondaryText = stylePreset.secondaryTextColor
    val cardShape = remember { RoundedCornerShape(20.dp) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        color = stylePreset.gradientColors.last(),
        shadowElevation = 6.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = stylePreset.specularBorderColors,
                start = Offset(0f, 0f),
                end = Offset(800f, 520f)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PHYSICAL_CARD_ASPECT_RATIO)
                .clip(cardShape)
        ) {
            LuxuryCardSurfaceLayer(
                stylePreset = stylePreset,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = bankName.ifBlank { "发卡银行" }.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cardAlias.ifBlank { "信用卡名称" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ContactlessWavesGraphic(waveColor = secondaryText)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmvChipGraphic(
                        useGoldAlloy = stylePreset.useGoldEmvChip,
                        compact = true
                    )
                    Text(
                        text = "••••  ••••  ••••  ${last4Digits.padEnd(4, '•').take(4)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontFeatureSettings = "tnum",
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = primaryText,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = stringResource(R.string.card_available_micro_label),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = primaryText.copy(alpha = 0.70f)
                        )
                        Text(
                            text = currency.format(creditLimit, trimWholeDecimals = true),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                                letterSpacing = (-0.3).sp
                            ),
                            color = primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "每月${repaymentDay}日还",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = secondaryText,
                            maxLines = 1
                        )
                    }
                    CardNetworkBadge(
                        network = network,
                        isLightSurface = isLightSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun FrostedWaiverPill(
    text: String,
    isWaived: Boolean,
    isLightSurface: Boolean
) {
    val surfaceColor = if (isLightSurface) {
        Color.White.copy(alpha = 0.48f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
    }
    val borderColor = when {
        isWaived && isLightSurface -> Color(0xFF059669).copy(alpha = 0.35f)
        isWaived -> Color(0xFF6EE7B7).copy(alpha = 0.45f)
        isLightSurface -> Color(0xFF0F172A).copy(alpha = 0.18f)
        else -> Color.White.copy(alpha = 0.32f)
    }
    val textColor = when {
        isWaived && isLightSurface -> Color(0xFF065F46)
        isWaived -> Color(0xFFA7F3D0)
        isLightSurface -> Color(0xFF1E293B)
        else -> Color.White.copy(alpha = 0.94f)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = surfaceColor,
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
            ),
            color = textColor,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun LuxuryCardSurfaceLayer(
    stylePreset: CardStylePreset,
    modifier: Modifier = Modifier
) {
    val isLight = stylePreset.isLightSurface
    val patternStrokeColor = if (isLight) {
        Color(0xFF0F172A).copy(alpha = 0.055f)
    } else {
        Color.White.copy(alpha = 0.065f)
    }
    val sheenColor = if (isLight) {
        Color.White.copy(alpha = 0.24f)
    } else {
        Color.White.copy(alpha = 0.07f)
    }

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = stylePreset.gradientColors,
                start = Offset(0f, 0f),
                end = Offset(950f, 650f)
            )
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Soft Radial Light-Bloom Orbs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stylePreset.accentBloomColor.copy(alpha = if (isLight) 0.42f else 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.86f, h * 0.14f),
                    radius = w * 0.56f
                ),
                radius = w * 0.56f,
                center = Offset(w * 0.86f, h * 0.14f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stylePreset.secondaryBloomColor.copy(alpha = if (isLight) 0.18f else 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.14f, h * 0.92f),
                    radius = w * 0.52f
                ),
                radius = w * 0.52f,
                center = Offset(w * 0.14f, h * 0.92f)
            )

            // 2. Diagonal Metallic / Frosted Specular Sheen Band
            val sheenPath = Path().apply {
                moveTo(w * 0.22f, 0f)
                lineTo(w * 0.54f, 0f)
                lineTo(w * 0.18f, h)
                lineTo(-w * 0.14f, h)
                close()
            }
            drawPath(
                path = sheenPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, sheenColor, Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(w * 0.55f, h)
                )
            )

            // 3. Procedural Geometric Texture (4% - 8% alpha)
            when (stylePreset.patternStyle) {
                CardPatternStyle.GUILLOCHE_ARCS -> drawGuillochePattern(patternStrokeColor)
                CardPatternStyle.TOPOGRAPHIC_WAVES -> drawTopographicWaves(patternStrokeColor)
                CardPatternStyle.BRUSHED_RADIAL -> drawBrushedMetallicFinish(patternStrokeColor)
            }
        }
    }
}

private fun DrawScope.drawGuillochePattern(strokeColor: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 0.85.dp.toPx())
    val center1 = Offset(w * 0.82f, h * 0.25f)
    for (i in 1..8) {
        drawCircle(
            color = strokeColor,
            radius = (w * 0.11f) * i,
            center = center1,
            style = stroke
        )
    }
    val center2 = Offset(w * 0.12f, h * 0.88f)
    for (i in 1..5) {
        drawCircle(
            color = strokeColor.copy(alpha = strokeColor.alpha * 0.75f),
            radius = (w * 0.10f) * i,
            center = center2,
            style = stroke
        )
    }
}

private fun DrawScope.drawTopographicWaves(strokeColor: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 0.9.dp.toPx(), cap = StrokeCap.Round)
    for (i in 0..6) {
        val baseY = h * (0.14f + i * 0.14f)
        val wavePath = Path().apply {
            moveTo(-w * 0.05f, baseY)
            cubicTo(
                w * 0.28f, baseY - h * 0.16f,
                w * 0.58f, baseY + h * 0.18f,
                w * 1.05f, baseY - h * 0.06f
            )
        }
        drawPath(
            path = wavePath,
            color = strokeColor,
            style = stroke
        )
    }
}

private fun DrawScope.drawBrushedMetallicFinish(strokeColor: Color) {
    val w = size.width
    val h = size.height
    val fineStroke = 0.6.dp.toPx()
    val lines = 18
    for (i in 0..lines) {
        val offset = (i / lines.toFloat())
        val startX = -w * 0.2f + offset * w * 1.3f
        drawLine(
            color = strokeColor.copy(alpha = strokeColor.alpha * 0.65f),
            start = Offset(startX, 0f),
            end = Offset(startX - w * 0.35f, h),
            strokeWidth = fineStroke
        )
    }
    val ringStroke = Stroke(width = 0.8.dp.toPx())
    val focal = Offset(w * 0.84f, h * 0.76f)
    for (i in 1..4) {
        drawCircle(
            color = strokeColor,
            radius = w * 0.14f * i,
            center = focal,
            style = ringStroke
        )
    }
}

@Composable
private fun EmvChipGraphic(
    useGoldAlloy: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val chipWidth = if (compact) 32.dp else 38.dp
    val chipHeight = if (compact) 23.dp else 28.dp
    val chipCorner = if (compact) 5.dp else 6.dp

    val metalGradient = if (useGoldAlloy) {
        listOf(
            Color(0xFFFDE68A),
            Color(0xFFF59E0B),
            Color(0xFFD97706),
            Color(0xFFFBBF24)
        )
    } else {
        listOf(
            Color(0xFFF1F5F9),
            Color(0xFFCBD5E1),
            Color(0xFF94A3B8),
            Color(0xFFE2E8F0)
        )
    }
    val grooveColor = if (useGoldAlloy) {
        Color(0xFF78350F).copy(alpha = 0.52f)
    } else {
        Color(0xFF334155).copy(alpha = 0.48f)
    }

    Box(
        modifier = modifier
            .size(chipWidth, chipHeight)
            .clip(RoundedCornerShape(chipCorner))
            .background(
                Brush.linearGradient(
                    colors = metalGradient,
                    start = Offset(0f, 0f),
                    end = Offset(120f, 90f)
                )
            )
            .border(0.6.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(chipCorner))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lineWidth = 0.9.dp.toPx()
            val padCorner = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())

            // Center contact module
            drawRoundRect(
                color = grooveColor,
                topLeft = Offset(w * 0.30f, h * 0.20f),
                size = Size(w * 0.40f, h * 0.60f),
                cornerRadius = padCorner,
                style = Stroke(width = lineWidth)
            )

            // Horizontal module dividers
            drawLine(
                color = grooveColor,
                start = Offset(0f, h * 0.36f),
                end = Offset(w * 0.30f, h * 0.36f),
                strokeWidth = lineWidth
            )
            drawLine(
                color = grooveColor,
                start = Offset(0f, h * 0.64f),
                end = Offset(w * 0.30f, h * 0.64f),
                strokeWidth = lineWidth
            )
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.70f, h * 0.36f),
                end = Offset(w, h * 0.36f),
                strokeWidth = lineWidth
            )
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.70f, h * 0.64f),
                end = Offset(w, h * 0.64f),
                strokeWidth = lineWidth
            )

            // Vertical central contact splits
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.50f, 0f),
                end = Offset(w * 0.50f, h * 0.20f),
                strokeWidth = lineWidth
            )
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.50f, h * 0.80f),
                end = Offset(w * 0.50f, h),
                strokeWidth = lineWidth
            )

            // Subtle inner specular highlight
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(w * 0.1f, h * 0.08f),
                end = Offset(w * 0.9f, h * 0.08f),
                strokeWidth = 0.7.dp.toPx()
            )
        }
    }
}

@Composable
private fun ContactlessWavesGraphic(
    waveColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(18.dp, 18.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        val centerY = size.height / 2f
        val originX = size.width * 0.14f
        val radii = listOf(
            size.width * 0.22f,
            size.width * 0.42f,
            size.width * 0.62f,
            size.width * 0.82f
        )
        radii.forEach { r ->
            drawArc(
                color = waveColor,
                startAngle = -42f,
                sweepAngle = 84f,
                useCenter = false,
                topLeft = Offset(originX - r * 0.55f, centerY - r),
                size = Size(r * 1.45f, r * 2f),
                style = stroke
            )
        }
    }
}
