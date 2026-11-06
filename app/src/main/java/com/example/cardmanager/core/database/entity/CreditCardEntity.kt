package com.example.cardmanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_cards",
    indices = [
        Index(value = ["last_4_digits"]),
        Index(value = ["repayment_day"])
    ]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "bank_name")
    val bankName: String,

    @ColumnInfo(name = "card_alias")
    val cardAlias: String,

    @ColumnInfo(name = "last_4_digits")
    val last4Digits: String,

    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double,

    @ColumnInfo(name = "bill_day")
    val billDay: Int,

    @ColumnInfo(name = "repayment_day")
    val repaymentDay: Int,

    @ColumnInfo(name = "card_color_hex")
    val cardColorHex: String = "#1E293B"
) {
    init {
        require(billDay in 1..31) { "billDay must be between 1 and 31" }
        require(repaymentDay in 1..31) { "repaymentDay must be between 1 and 31" }
        require(last4Digits.length == 4 && last4Digits.all { it.isDigit() }) {
            "last4Digits must be exactly 4 digits"
        }
        require(creditLimit >= 0.0) { "creditLimit cannot be negative" }
    }
}
