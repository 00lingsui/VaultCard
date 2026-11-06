package com.example.cardmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cardmanager.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE card_id = :cardId ORDER BY date_timestamp DESC")
    fun getTransactionsByCard(cardId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date_timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions")
    fun getTotalDebt(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity): Int
}
