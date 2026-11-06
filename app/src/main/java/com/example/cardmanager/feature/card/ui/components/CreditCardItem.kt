package com.example.cardmanager.feature.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.core.common.util.calculateDaysUntilRepayment
import com.example.cardmanager.core.database.entity.CreditCardEntity
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardItem(
    card: CreditCardEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val daysRemaining = card.calculateDaysUntilRepayment()
    val baseColor = try {
        Color(android.graphics.Color.parseColor(card.cardColorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(baseColor, baseColor.copy(alpha = 0.75f))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = card.bankName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = card.cardAlias,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Surface(
                        color = when {
                            daysRemaining <= 3L -> Color(0xFFEF4444)
                            daysRemaining <= 7L -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (daysRemaining == 0L) "Due Today!" else "$daysRemaining days left",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "••••  ••••  ••••  ${card.last4Digits}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CREDIT LIMIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.US).format(card.creditLimit),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Due: ${card.repaymentDay}th",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
