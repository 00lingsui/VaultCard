package com.example.cardmanager.feature.card.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.R
import com.example.cardmanager.core.common.util.calculateDaysUntilRepayment
import com.example.cardmanager.core.database.entity.CardNetwork
import com.example.cardmanager.core.database.entity.CardStylePreset
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardItem(
    card: CreditCardEntity,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    onClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null
) {
    val themeState = LocalThemeState.current
    val isMiui = themeState.currentStyle == AppThemeStyle.MIUIX
    val cornerRadius = if (isMiui) 26.dp else 20.dp

    var expanded by remember { mutableStateOf(initiallyExpanded) }

    val daysRemaining = card.calculateDaysUntilRepayment()
    val stylePreset = remember(card.cardStyle, card.cardColorHex) {
        CardStylePreset.resolve(card.cardStyle, card.cardColorHex)
    }
    val isLightSurface = stylePreset.isLightSurface
    val primaryText = stylePreset.primaryTextColor
    val secondaryText = stylePreset.secondaryTextColor
    val gradientColors = stylePreset.gradientColors

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            expanded -> 1.01f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "cardScale"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 2.dp
            expanded -> 12.dp
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { expanded = !expanded }
            ),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = if (isLightSurface) {
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color(0xFF94A3B8).copy(alpha = 0.35f),
                        Color(0xFF64748B).copy(alpha = 0.25f)
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = if (expanded) 0.5f else 0.35f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Black.copy(alpha = 0.25f)
                    )
                },
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(700f, 600f)
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = if (isLightSurface) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.95f, size.height * 0.15f)
                )
                drawCircle(
                    color = if (isLightSurface) Color(0xFF0F172A).copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.12f),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.1f, size.height * 1.1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                // Top Row: Bank Name, Alias & Due Countdown Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.bankName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.6.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = secondaryText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = card.cardAlias,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = if (isMiui) (-0.4).sp else 0.sp
                            ),
                            color = primaryText
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
                            color = badgeBg.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = badgeText,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "展开卡片",
                            tint = secondaryText,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Middle: EMV Chip & Contactless Waves + Card Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EmvChipGraphic()
                        ContactlessWavesGraphic(waveColor = secondaryText)
                    }

                    Text(
                        text = "••••  ••••  ••••  ${card.last4Digits}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.6.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = primaryText
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Bottom Row: Credit Limit and Due Day + Dynamic Card Network Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "信用额度",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = secondaryText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.CHINA).format(card.creditLimit),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = primaryText
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "还款日",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = secondaryText
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "每月 ${card.repaymentDay} 日",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = primaryText
                            )
                        }

                        CardNetworkBadge(
                            network = card.cardNetwork,
                            isLightSurface = isLightSurface
                        )
                    }
                }

                // Spring-Animated Expanded Details Section
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(spring(dampingRatio = 0.8f)) + expandVertically(spring(dampingRatio = 0.8f, stiffness = 300f)),
                    exit = fadeOut() + shrinkVertically(spring(dampingRatio = 0.8f, stiffness = 300f))
                ) {
                    Column(modifier = Modifier.padding(top = 18.dp)) {
                        HorizontalDivider(
                            color = if (isLightSurface) Color(0xFF0F172A).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.16f),
                            thickness = 1.dp
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
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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

                        val feeStr = if (card.annualFeeAmount % 1.0 == 0.0) {
                            card.annualFeeAmount.toInt().toString()
                        } else {
                            card.annualFeeAmount.toString()
                        }

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
                                    "¥$feeStr"
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
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
                                    card.annualFeeWaiverCount > 0 -> stringResource(R.string.annual_fee_waivable_summary, feeStr, card.annualFeeWaiverCount)
                                    else -> stringResource(R.string.annual_fee_rigid_summary, feeStr)
                                }
                                Text(
                                    text = waiverDetailText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
                                onClick = onClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = stylePreset.subtleSurfaceOverlay,
                                    contentColor = primaryText
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
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
                                    onClick = onDeleteClick,
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
    modifier: Modifier = Modifier
) {
    val isLightSurface = stylePreset.isLightSurface
    val primaryText = stylePreset.primaryTextColor
    val secondaryText = stylePreset.secondaryTextColor

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            1.dp,
            if (isLightSurface) Color(0xFF94A3B8).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.22f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = stylePreset.gradientColors))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bankName.ifBlank { "发卡银行" }.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                            color = secondaryText
                        )
                        Text(
                            text = cardAlias.ifBlank { "信用卡名称" },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryText
                        )
                    }
                    EmvChipGraphic()
                }

                Text(
                    text = "••••  ••••  ••••  ${last4Digits.padEnd(4, '•').take(4)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = primaryText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "额度 ${NumberFormat.getCurrencyInstance(Locale.CHINA).format(creditLimit)} · 每月${repaymentDay}日还",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText
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
private fun EmvChipGraphic(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp, 26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD54F),
                        Color(0xFFF59E0B),
                        Color(0xFFD97706)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeColor = Color(0xFF78350F).copy(alpha = 0.5f)
            val strokeWidth = 1.dp.toPx()

            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                size = size,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(1.dp.toPx())
            )
            drawLine(
                color = strokeColor,
                start = Offset(0f, size.height * 0.5f),
                end = Offset(size.width, size.height * 0.5f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = strokeColor,
                start = Offset(size.width * 0.35f, 0f),
                end = Offset(size.width * 0.35f, size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = strokeColor,
                start = Offset(size.width * 0.65f, 0f),
                end = Offset(size.width * 0.65f, size.height),
                strokeWidth = strokeWidth
            )
            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(size.width * 0.25f, size.height * 0.25f),
                size = Size(size.width * 0.5f, size.height * 0.5f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(strokeWidth)
            )
        }
    }
}

@Composable
private fun ContactlessWavesGraphic(
    waveColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(16.dp, 20.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx())
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, 2.dp.toPx()),
            size = Size(10.dp.toPx(), 16.dp.toPx()),
            style = stroke
        )
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(4.dp.toPx(), 0f),
            size = Size(14.dp.toPx(), 20.dp.toPx()),
            style = stroke
        )
    }
}
