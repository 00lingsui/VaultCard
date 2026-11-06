package com.example.cardmanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["card_id"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "card_id")
    val cardId: Long,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "note")
    val note: String = "",

    @ColumnInfo(name = "date_timestamp")
    val dateTimestamp: Long = System.currentTimeMillis()
)
