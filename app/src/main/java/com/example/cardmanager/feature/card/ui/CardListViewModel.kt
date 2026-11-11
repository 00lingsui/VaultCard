package com.example.cardmanager.feature.card.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity
import com.example.cardmanager.feature.card.data.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardListUiState(
    val cards: List<CreditCardEntity> = emptyList(),
    val totalCreditLimit: Double = 0.0,
    val totalDebt: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val repository: CreditCardRepository
) : ViewModel() {

    val uiState: StateFlow<CardListUiState> = combine(
        repository.getAllCards(),
        repository.getTotalDebt()
    ) { cards, totalDebt ->
        val sharedLimit = cards.groupBy { it.bankName.trim() }
            .values
            .sumOf { bankCards -> bankCards.maxOfOrNull { it.creditLimit } ?: 0.0 }

        CardListUiState(
            cards = cards,
            totalCreditLimit = sharedLimit,
            totalDebt = totalDebt,
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
