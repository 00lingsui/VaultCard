package com.example.cardmanager.feature.card.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardmanager.core.currency.AppCurrency
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity
import com.example.cardmanager.feature.card.data.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class CardListUiState(
    val cards: List<CreditCardEntity> = emptyList(),
    val totalCreditLimit: Double = 0.0,
    val totalDebt: Double = 0.0,
    val limitByCurrency: Map<String, Double> = emptyMap(),
    val debtByCurrency: Map<String, Double> = emptyMap(),
    val annualSwipeCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val repository: CreditCardRepository
) : ViewModel() {

    val uiState: StateFlow<CardListUiState> = combine(
        repository.getAllCards(),
        repository.getAllTransactions(),
        repository.getTotalDebt(),
        repository.getQualifyingSwipeCountsForCurrentYear()
    ) { cards, transactions, totalDebt, swipeCounts ->
        val limitByCurrency = cards
            .groupBy { it.currencyCode }
            .mapValues { (_, currencyCards) ->
                currencyCards
                    .groupBy { it.bankName.trim().lowercase(Locale.ROOT) }
                    .values
                    .sumOf { bankCards -> bankCards.maxOfOrNull { it.creditLimit } ?: 0.0 }
            }

        val cardCurrencyMap = cards.associate { it.id to it.currencyCode }
        val debtByCurrency = mutableMapOf<String, Double>()
        transactions.forEach { tx ->
            val code = cardCurrencyMap[tx.cardId] ?: AppCurrency.CNY.code
            debtByCurrency[code] = (debtByCurrency[code] ?: 0.0) + tx.amount
        }

        val sharedLimit = limitByCurrency.values.sum()

        CardListUiState(
            cards = cards,
            totalCreditLimit = sharedLimit,
            totalDebt = totalDebt,
            limitByCurrency = limitByCurrency,
            debtByCurrency = debtByCurrency,
            annualSwipeCounts = swipeCounts,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CardListUiState(isLoading = true)
    )

    fun addCard(card: CreditCardEntity) {
        viewModelScope.launch { repository.addCard(card) }
    }

    fun updateCard(card: CreditCardEntity) {
        viewModelScope.launch { repository.updateCard(card) }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch { repository.deleteCard(cardId) }
    }

    fun addTransaction(cardId: Long, amount: Double, category: String, note: String) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(cardId = cardId, amount = amount, category = category, note = note)
            )
        }
    }
}
