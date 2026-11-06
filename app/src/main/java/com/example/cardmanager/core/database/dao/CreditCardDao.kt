package com.example.cardmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.cardmanager.core.database.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {

    @Query("SELECT * FROM credit_cards ORDER BY repayment_day ASC")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    fun getCardById(id: Long): Flow<CreditCardEntity?>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCardByIdOnce(id: Long): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity): Long

    @Upsert
    suspend fun upsertCard(card: CreditCardEntity): Long

    @Update
    suspend fun updateCard(card: CreditCardEntity): Int

    @Delete
    suspend fun deleteCard(card: CreditCardEntity): Int

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long): Int

    @Query("SELECT COUNT(*) FROM credit_cards")
    fun getCardCount(): Flow<Int>
}
