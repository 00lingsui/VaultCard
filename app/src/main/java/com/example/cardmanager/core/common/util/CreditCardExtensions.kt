package com.example.cardmanager.core.common.util

import com.example.cardmanager.core.database.entity.CreditCardEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

fun CreditCardEntity.calculateDaysUntilRepayment(
    currentDate: LocalDate = LocalDate.now()
): Long {
    val currentYearMonth = YearMonth.from(currentDate)
    val safeRepaymentDayThisMonth = minOf(this.repaymentDay, currentYearMonth.lengthOfMonth())
    val repaymentDateThisMonth = currentYearMonth.atDay(safeRepaymentDayThisMonth)

    val nextRepaymentDate = if (!currentDate.isAfter(repaymentDateThisMonth)) {
        repaymentDateThisMonth
    } else {
        val nextYearMonth = currentYearMonth.plusMonths(1)
        val safeRepaymentDayNextMonth = minOf(this.repaymentDay, nextYearMonth.lengthOfMonth())
        nextYearMonth.atDay(safeRepaymentDayNextMonth)
    }

    return ChronoUnit.DAYS.between(currentDate, nextRepaymentDate)
}

fun CreditCardEntity.getNextRepaymentDate(
    currentDate: LocalDate = LocalDate.now()
): LocalDate {
    val currentYearMonth = YearMonth.from(currentDate)
    val safeRepaymentDayThisMonth = minOf(this.repaymentDay, currentYearMonth.lengthOfMonth())
    val repaymentDateThisMonth = currentYearMonth.atDay(safeRepaymentDayThisMonth)

    return if (!currentDate.isAfter(repaymentDateThisMonth)) {
        repaymentDateThisMonth
    } else {
        val nextYearMonth = currentYearMonth.plusMonths(1)
        val safeRepaymentDayNextMonth = minOf(this.repaymentDay, nextYearMonth.lengthOfMonth())
        nextYearMonth.atDay(safeRepaymentDayNextMonth)
    }
}
