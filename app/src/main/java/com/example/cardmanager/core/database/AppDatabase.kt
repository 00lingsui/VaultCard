package com.example.cardmanager.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.dao.TransactionDao
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity

@Database(
    entities = [CreditCardEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "vault_card.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
