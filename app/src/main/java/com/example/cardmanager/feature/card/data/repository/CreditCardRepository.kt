package com.example.cardmanager.feature.card.data.repository

import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.dao.TransactionDao
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface CreditCardRepository {
    fun getAllCards(): Flow<List<CreditCardEntity>>
    fun getCardById(id: Long): Flow<CreditCardEntity?>
    suspend fun addCard(card: CreditCardEntity): Long
    suspend fun updateCard(card: CreditCardEntity): Boolean
    suspend fun deleteCard(cardId: Long): Boolean
    fun getTransactions(cardId: Long): Flow<List<TransactionEntity>>
    fun getTotalDebt(): Flow<Double>
    suspend fun addTransaction(transaction: TransactionEntity): Long
}

@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val cardDao: CreditCardDao,
    private val transactionDao: TransactionDao
) : CreditCardRepository {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getAllCards(): Flow<List<CreditCardEntity>> =
        cardDao.getAllCards().flowOn(ioDispatcher)

    override fun getCardById(id: Long): Flow<CreditCardEntity?> =
        cardDao.getCardById(id).flowOn(ioDispatcher)

    override suspend fun addCard(card: CreditCardEntity): Long =
        withContext(ioDispatcher) { cardDao.insertCard(card) }

    override suspend fun updateCard(card: CreditCardEntity): Boolean =
        withContext(ioDispatcher) { cardDao.updateCard(card) > 0 }

    override suspend fun deleteCard(cardId: Long): Boolean =
        withContext(ioDispatcher) { cardDao.deleteCardById(cardId) > 0 }

    override fun getTransactions(cardId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCard(cardId).flowOn(ioDispatcher)

    override fun getTotalDebt(): Flow<Double> =
        transactionDao.getTotalDebt().flowOn(ioDispatcher)

    override suspend fun addTransaction(transaction: TransactionEntity): Long =
        withContext(ioDispatcher) { transactionDao.insertTransaction(transaction) }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCreditCardRepository(
        impl: CreditCardRepositoryImpl
    ): CreditCardRepository
}
