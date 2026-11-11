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
        require(billDay in 1..31) { "账单日必须在1到31日之间" }
        require(repaymentDay in 1..31) { "还款日必须在1到31日之间" }
        require(last4Digits.length == 4 && last4Digits.all { it.isDigit() }) {
            "卡号后4位必须为4位数字"
        }
        require(creditLimit >= 0.0) { "信用额度不能为负数" }
    }
}
