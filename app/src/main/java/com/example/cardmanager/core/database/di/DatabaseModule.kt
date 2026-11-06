package com.example.cardmanager.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.cardmanager.core.database.AppDatabase
import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCreditCardDao(database: AppDatabase): CreditCardDao {
        return database.creditCardDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
}
