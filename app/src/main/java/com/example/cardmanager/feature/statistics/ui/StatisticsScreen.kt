package com.example.cardmanager.feature.statistics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeState = LocalThemeState.current
    val isMiui = themeState.currentStyle == AppThemeStyle.MIUIX
    val cardRadius = if (isMiui) 24.dp else 16.dp
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "财务统计与分析",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (isMiui) (-0.5).sp else 0.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top KPI Summary Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiSummaryCard(
                            title = "待还款总额",
                            value = currencyFormat.format(uiState.totalDebt),
                            valueColor = if (uiState.totalDebt > 0) Color(0xFFFF5252) else Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            shapeRadius = cardRadius
                        )
                        KpiSummaryCard(
                            title = "单笔最大支出",
                            value = currencyFormat.format(uiState.largestExpense),
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            shapeRadius = cardRadius
                        )
                    }
                }

                // 1. Credit Utilization Donut Chart
                item {
                    ChartContainerCard(
                        title = "额度使用占比",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        shapeRadius = cardRadius
                    ) {
                        DonutChart(
                            totalLimit = uiState.totalLimit,
                            totalDebt = uiState.totalDebt,
                            debtColor = Color(0xFFFF5252),
                            availableColor = Color(0xFF10B981),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // 2. Spending Trend Line Chart
                item {
                    ChartContainerCard(
                        title = "近期支出趋势",
                        icon = Icons.Outlined.ShowChart,
                        shapeRadius = cardRadius
                    ) {
                        LineChart(
                            dataPoints = uiState.trendPoints,
                            labels = uiState.trendLabels,
                            lineColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // 3. Category Spending Breakdown Pie Chart
                item {
                    ChartContainerCard(
                        title = "消费分类占比",
                        icon = Icons.Outlined.PieChart,
                        shapeRadius = cardRadius
                    ) {
                        PieChart(
                            slices = uiState.categorySlices,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Bottom Spacing
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun KpiSummaryCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    shapeRadius: androidx.compose.ui.unit.Dp = 16.dp
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(shapeRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = valueColor
            )
        }
    }
}

@Composable
private fun ChartContainerCard(
    title: String,
    icon: ImageVector,
    shapeRadius: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(shapeRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}
