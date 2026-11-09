package com.example.cardmanager.feature.card.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyCardsState(
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMiui: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val cornerRadius = if (isMiui) 28.dp else 22.dp

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(600)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(600)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Sleek Layered Cards Illustration
                Box(
                    modifier = Modifier
                        .size(160.dp, 110.dp)
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Back card (angled)
                    Box(
                        modifier = Modifier
                            .size(130.dp, 80.dp)
                            .rotate(-10f)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        Color(0xFF1E2430).copy(alpha = 0.7f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                primaryColor.copy(alpha = 0.35f),
                                RoundedCornerShape(14.dp)
                            )
                    )

                    // Front card (canvas detail: chip + embossed magnetic strip)
                    Box(
                        modifier = Modifier
                            .size(140.dp, 88.dp)
                            .rotate(4f)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF263248),
                                        Color(0xFF161B26)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.18f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Card Chip
                            drawRoundRect(
                                color = Color(0xFFF59E0B).copy(alpha = 0.75f),
                                topLeft = Offset(16.dp.toPx(), 18.dp.toPx()),
                                size = Size(22.dp.toPx(), 16.dp.toPx()),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            // Decorative Lines
                            drawLine(
                                color = Color.White.copy(alpha = 0.3f),
                                start = Offset(16.dp.toPx(), 54.dp.toPx()),
                                end = Offset(70.dp.toPx(), 54.dp.toPx()),
                                strokeWidth = 3.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(16.dp.toPx(), 64.dp.toPx()),
                                end = Offset(45.dp.toPx(), 64.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 2. Strong Visual Hierarchy: Title & Subtitle
                Text(
                    text = "No Cards Added Yet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (isMiui) (-0.5).sp else 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Tap the + button below to log your first credit card and enable smart repayment tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Action Callout Button
                FilledTonalButton(
                    onClick = onAddCardClick,
                    shape = RoundedCornerShape(if (isMiui) 16.dp else 12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Add Card",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
