package com.example.cardmanager.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cardmanager.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CardSwipeCount(
    @ColumnInfo(name = "card_id")
    val cardId: Long,
    @ColumnInfo(name = "swipe_count")
    val swipeCount: Int
)

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE card_id = :cardId ORDER BY date_timestamp DESC")
    fun getTransactionsByCard(cardId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date_timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions")
    fun getTotalDebt(): Flow<Double>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM transactions
        WHERE card_id = :cardId
          AND date_timestamp >= :startTimestamp
          AND date_timestamp <= :endTimestamp
          AND amount > 0
          AND category NOT IN ('还款', '信用卡还款', '退款', '消费退款', 'Repayment', 'Refund')
        """
    )
    fun getQualifyingSwipeCountInRange(
        cardId: Long,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<Int>

    @Query(
        """
        SELECT card_id, COUNT(*) AS swipe_count FROM transactions
        WHERE date_timestamp >= :startTimestamp
          AND date_timestamp <= :endTimestamp
          AND amount > 0
          AND category NOT IN ('还款', '信用卡还款', '退款', '消费退款', 'Repayment', 'Refund')
        GROUP BY card_id
        """
    )
    fun getQualifyingSwipeCountsByCardInRange(
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<CardSwipeCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity): Int
}
