package com.example.cardmanager.feature.statistics.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardmanager.feature.card.data.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StatisticsUiState(
    val totalLimit: Double = 0.0,
    val totalDebt: Double = 0.0,
    val availableCredit: Double = 0.0,
    val totalTransactionsCount: Int = 0,
    val largestExpense: Double = 0.0,
    val categorySlices: List<PieChartSlice> = emptyList(),
    val trendPoints: List<Float> = emptyList(),
    val trendLabels: List<String> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: CreditCardRepository
) : ViewModel() {

    private val categoryPalette = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFEC4899), // Pink
        Color(0xFF8B5CF6), // Purple
        Color(0xFF14B8A6), // Teal
        Color(0xFFEF4444)  // Red
    )

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.getAllCards(),
        repository.getAllTransactions(),
        repository.getTotalDebt()
    ) { cards, transactions, totalDebt ->
        val totalLimit = cards.sumOf { it.creditLimit }
        val available = (totalLimit - totalDebt).coerceAtLeast(0.0)

        // 1. Category aggregation
        val categoryTotals = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val categorySlices = categoryTotals.entries.mapIndexed { index, entry ->
            PieChartSlice(
                label = entry.key,
                value = entry.value,
                color = categoryPalette.getOrElse(index) { Color(0xFF64748B) }
            )
        }

        // 2. Trend points (group by date)
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val dateGrouped = transactions
            .sortedBy { it.dateTimestamp }
            .groupBy { dateFormat.format(Date(it.dateTimestamp)) }

        val trendPoints = if (dateGrouped.isNotEmpty()) {
            dateGrouped.values.map { list -> list.sumOf { it.amount }.toFloat() }
        } else {
            emptyList()
        }

        val trendLabels = if (dateGrouped.isNotEmpty()) {
            dateGrouped.keys.toList()
        } else {
            emptyList()
        }

        val largest = transactions.maxOfOrNull { it.amount } ?: 0.0

        StatisticsUiState(
            totalLimit = totalLimit,
            totalDebt = totalDebt,
            availableCredit = available,
            totalTransactionsCount = transactions.size,
            largestExpense = largest,
            categorySlices = categorySlices,
            trendPoints = trendPoints,
            trendLabels = trendLabels,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(isLoading = true)
    )
}
