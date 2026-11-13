package com.example.cardmanager.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.dao.TransactionDao
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity

@Database(
    entities = [CreditCardEntity::class, TransactionEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(CardConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "vault_card.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN has_annual_fee INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN annual_fee_amount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN annual_fee_waiver_count INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN card_network TEXT NOT NULL DEFAULT 'UNIONPAY'")
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN card_style TEXT NOT NULL DEFAULT 'DEEP_OCEAN'")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
