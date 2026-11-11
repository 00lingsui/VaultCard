package com.example.cardmanager.feature.statistics.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

data class PieChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun DonutChart(
    totalLimit: Double,
    totalDebt: Double,
    modifier: Modifier = Modifier,
    debtColor: Color = Color(0xFFFF5252),
    availableColor: Color = Color(0xFF10B981),
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(totalDebt, totalLimit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val available = (totalLimit - totalDebt).coerceAtLeast(0.0)
    val debtRatio = if (totalLimit > 0) (totalDebt / totalLimit).toFloat().coerceIn(0f, 1f) else 0f
    val debtSweep = debtRatio * 360f * animationProgress.value
    val availableSweep = (1f - debtRatio) * 360f * animationProgress.value

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 22.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                )
                val arcSize = Size(radius * 2, radius * 2)

                // Background Track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                // Available Credit Arc
                if (availableSweep > 0f) {
                    drawArc(
                        color = availableColor,
                        startAngle = -90f + debtSweep,
                        sweepAngle = availableSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Debt / Outstanding Arc
                if (debtSweep > 0f) {
                    drawArc(
                        color = debtColor,
                        startAngle = -90f,
                        sweepAngle = debtSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(debtRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "已使用",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChartLegendItem(
                color = debtColor,
                label = "待还款",
                value = currencyFormat.format(totalDebt)
            )
            ChartLegendItem(
                color = availableColor,
                label = "可用额度",
                value = currencyFormat.format(available)
            )
        }
    }
}

@Composable
fun LineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无支出历史记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        progress.animateTo(1f, animationSpec = tween(900))
    }

    val maxValue = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(10f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 12.dp, vertical = 8.dp)) {
            val width = size.width
            val height = size.height
            val spacing = width / (dataPoints.size - 1).coerceAtLeast(1)

            val coordinates = dataPoints.mapIndexed { index, value ->
                val x = index * spacing
                val normalizedY = 1f - ((value / maxValue) * progress.value).coerceIn(0f, 1f)
                val y = normalizedY * (height - 30f) + 15f
                Offset(x, y)
            }

            // Draw Background Gradient Fill Path
            val fillPath = Path().apply {
                if (coordinates.isNotEmpty()) {
                    moveTo(coordinates.first().x, height)
                    lineTo(coordinates.first().x, coordinates.first().y)
                    for (i in 1 until coordinates.size) {
                        lineTo(coordinates[i].x, coordinates[i].y)
                    }
                    lineTo(coordinates.last().x, height)
                    close()
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor, Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Stroke Path
            val strokePath = Path().apply {
                if (coordinates.isNotEmpty()) {
                    moveTo(coordinates.first().x, coordinates.first().y)
                    for (i in 1 until coordinates.size) {
                        lineTo(coordinates[i].x, coordinates[i].y)
                    }
                }
            }

            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Data Point Dots
            coordinates.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }

        // Horizontal X-axis labels
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(
    slices: List<PieChartSlice>,
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无消费分类记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val total = slices.sumOf { it.value }.coerceAtLeast(1.0)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Pie Canvas
        Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = ((slice.value / total) * 360f).toFloat() * animationProgress.value
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = size
                    )
                    startAngle += sweepAngle
                }
            }
        }

        // Category Legend List
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            slices.forEach { slice ->
                val pct = ((slice.value / total) * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).background(slice.color, CircleShape))
                    Column {
                        Text(
                            text = "${slice.label} ($pct%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currencyFormat.format(slice.value),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
