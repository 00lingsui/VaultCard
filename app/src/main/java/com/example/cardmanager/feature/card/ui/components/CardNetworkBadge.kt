package com.example.cardmanager.feature.card.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.core.database.entity.CardNetwork

@Composable
fun CardNetworkBadge(
    network: CardNetwork,
    isLightSurface: Boolean,
    modifier: Modifier = Modifier
) {
    when (network) {
        CardNetwork.UNIONPAY -> UnionPayBadge(modifier = modifier)
        CardNetwork.VISA -> VisaBadge(isLightSurface = isLightSurface, modifier = modifier)
        CardNetwork.MASTERCARD -> MastercardBadge(modifier = modifier)
        CardNetwork.AMEX -> AmexBadge(modifier = modifier)
        CardNetwork.JCB -> JcbBadge(modifier = modifier)
        CardNetwork.UNIONPAY_VISA -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UnionPayBadge()
                VisaBadge(isLightSurface = isLightSurface)
            }
        }
        CardNetwork.UNIONPAY_MASTERCARD -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UnionPayBadge()
                MastercardBadge()
            }
        }
        CardNetwork.OTHER -> OtherNetworkBadge(isLightSurface = isLightSurface, modifier = modifier)
    }
}

@Composable
private fun UnionPayBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(22.dp)
            .width(36.dp)
            .clip(RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val slant = w * 0.12f

            // Red left trapezoid
            drawPath(
                path = Path().apply {
                    moveTo(slant, 0f)
                    lineTo(w * 0.42f, 0f)
                    lineTo(w * 0.30f, h)
                    lineTo(0f, h)
                    close()
                },
                color = Color(0xFFE21836)
            )
            // Deep blue center parallelogram
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.42f, 0f)
                    lineTo(w * 0.72f, 0f)
                    lineTo(w * 0.60f, h)
                    lineTo(w * 0.30f, h)
                    close()
                },
                color = Color(0xFF00447C)
            )
            // Teal right trapezoid
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.72f, 0f)
                    lineTo(w, 0f)
                    lineTo(w - slant, h)
                    lineTo(w * 0.60f, h)
                    close()
                },
                color = Color(0xFF007B84)
            )
        }
        Text(
            text = "银联",
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 9.sp
        )
    }
}

@Composable
private fun VisaBadge(
    isLightSurface: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "VISA",
            color = if (isLightSurface) Color(0xFF1A1F71) else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun MastercardBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(36.dp, 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = 10.dp.toPx()
            drawCircle(
                color = Color(0xFFEB001B).copy(alpha = 0.92f),
                radius = radius,
                center = Offset(11.dp.toPx(), size.height / 2)
            )
            drawCircle(
                color = Color(0xFFF79E1B).copy(alpha = 0.88f),
                radius = radius,
                center = Offset(22.dp.toPx(), size.height / 2)
            )
        }
    }
}

@Composable
private fun AmexBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(22.dp),
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF006FCF),
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AMEX",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun JcbBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(22.dp)
            .width(34.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF0E4C96)))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFC4161C)))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF007B3E)))
        }
        Text(
            text = "JCB",
            color = Color.White,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun OtherNetworkBadge(
    isLightSurface: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(22.dp),
        shape = RoundedCornerShape(5.dp),
        color = if (isLightSurface) Color(0xFF1E293B).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.18f),
        border = BorderStroke(
            width = 0.75.dp,
            color = if (isLightSurface) Color(0xFF1E293B).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CARD",
                color = if (isLightSurface) Color(0xFF1E293B) else Color.White,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}
