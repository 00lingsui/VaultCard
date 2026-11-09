package com.example.cardmanager.feature.card.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.core.common.util.calculateDaysUntilRepayment
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardItem(
    card: CreditCardEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val themeState = LocalThemeState.current
    val isMiui = themeState.currentStyle == AppThemeStyle.MIUIX
    val cornerRadius = if (isMiui) 26.dp else 20.dp

    val daysRemaining = card.calculateDaysUntilRepayment()
    val baseColor = try {
        Color(android.graphics.Color.parseColor(card.cardColorHex))
    } catch (_: Exception) {
        Color(0xFF2563EB)
    }

    // Micro-interaction: tactile scale bounce on press
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "cardScale"
    )

    // Palette with dark-mode richness
    val gradientColors = listOf(
        baseColor,
        baseColor.copy(alpha = 0.88f),
        Color(0xFF0F172A)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f),
                    Color.Black.copy(alpha = 0.25f)
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(700f, 600f)
                    )
                )
        ) {
            // Subtle ambient card background decorative pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.95f, size.height * 0.15f)
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.12f),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.1f, size.height * 1.1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
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
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = card.cardAlias,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = if (isMiui) (-0.4).sp else 0.sp
                            ),
                            color = Color.White
                        )
                    }

                    // Urgency-aware Countdown Pill
                    val (badgeBg, dotColor, badgeText) = when {
                        daysRemaining <= 0L -> Triple(Color(0xFFDC2626), Color(0xFFFF8A80), "Due Today!")
                        daysRemaining <= 3L -> Triple(Color(0xFFEF4444), Color(0xFFFFB4AB), "$daysRemaining days left")
                        daysRemaining <= 7L -> Triple(Color(0xFFD97706), Color(0xFFFFD54F), "$daysRemaining days left")
                        else -> Triple(Color(0xFF059669), Color(0xFFA7F3D0), "$daysRemaining days left")
                    }

                    Surface(
                        color = badgeBg.copy(alpha = 0.88f),
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
                }

                // Middle: Realistic EMV Chip & Contactless Waves + Card Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brushed Gold EMV Chip with Contactless Arcs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EmvChipGraphic()
                        ContactlessWavesGraphic()
                    }

                    // Masked Monospaced Card Number
                    Text(
                        text = "••••  ••••  ••••  ${card.last4Digits}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.8.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }

                // Bottom Row: Credit Limit and Due Day + Payment Network Monogram
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CREDIT LIMIT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.US).format(card.creditLimit),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DUE DAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White.copy(alpha = 0.65f)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${card.repaymentDay}th of month",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Stylized Card Network Monogram (Overlapping glowing circles)
                        PaymentNetworkGraphic()
                    }
                }
            }
        }
    }
}

/**
 * High-fidelity Vector Graphic of a Smart EMV Card Chip
 */
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

            // Outer border
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                size = size,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(1.dp.toPx())
            )

            // Inner circuit lines
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

/**
 * Contactless Radio Wave Graphic
 */
@Composable
private fun ContactlessWavesGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp, 20.dp)) {
        val waveColor = Color.White.copy(alpha = 0.75f)
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

/**
 * Stylized Overlapping circles representing premium payment brand
 */
@Composable
private fun PaymentNetworkGraphic(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(36.dp, 22.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = 10.dp.toPx()
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.85f),
                radius = radius,
                center = Offset(11.dp.toPx(), size.height / 2)
            )
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.75f),
                radius = radius,
                center = Offset(21.dp.toPx(), size.height / 2)
            )
        }
    }
}
