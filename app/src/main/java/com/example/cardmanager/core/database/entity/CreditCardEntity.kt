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
    val cardColorHex: String = "#1E293B",

    @ColumnInfo(name = "has_annual_fee", defaultValue = "0")
    val hasAnnualFee: Boolean = false,

    @ColumnInfo(name = "annual_fee_amount", defaultValue = "0.0")
    val annualFeeAmount: Double = 0.0,

    @ColumnInfo(name = "annual_fee_waiver_count", defaultValue = "0")
    val annualFeeWaiverCount: Int = 0
) {
    init {
        require(billDay in 1..31) { "账单日必须在1到31日之间" }
        require(repaymentDay in 1..31) { "还款日必须在1到31日之间" }
        require(last4Digits.length == 4 && last4Digits.all { it.isDigit() }) {
            "卡号后4位必须为4位数字"
        }
        require(creditLimit >= 0.0) { "信用额度不能为负数" }
        require(annualFeeAmount >= 0.0) { "年费金额不能为负数" }
        require(annualFeeWaiverCount >= 0) { "刷卡免年费次数不能为负数" }
    }
}
