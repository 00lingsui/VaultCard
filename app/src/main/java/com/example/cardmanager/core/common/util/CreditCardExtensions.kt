package com.example.cardmanager.core.common.util

import com.example.cardmanager.core.database.entity.CreditCardEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

fun CreditCardEntity.calculateDaysUntilRepayment(
    currentDate: LocalDate = LocalDate.now()
): Long {
    val nextRepaymentDate = getNextRepaymentDate(currentDate)
    return ChronoUnit.DAYS.between(currentDate, nextRepaymentDate)
}

fun CreditCardEntity.getNextRepaymentDate(
    currentDate: LocalDate = LocalDate.now()
): LocalDate = nextOccurrenceOfMonthDay(repaymentDay, currentDate)

fun CreditCardEntity.calculateDaysUntilStatement(
    currentDate: LocalDate = LocalDate.now()
): Long {
    val nextStatementDate = getNextStatementDate(currentDate)
    return ChronoUnit.DAYS.between(currentDate, nextStatementDate)
}

fun CreditCardEntity.getNextStatementDate(
    currentDate: LocalDate = LocalDate.now()
): LocalDate = nextOccurrenceOfMonthDay(billDay, currentDate)

private fun nextOccurrenceOfMonthDay(
    targetDay: Int,
    currentDate: LocalDate
): LocalDate {
    val clampedDay = targetDay.coerceIn(1, 31)
    val currentYearMonth = YearMonth.from(currentDate)
    val safeDayThisMonth = minOf(clampedDay, currentYearMonth.lengthOfMonth())
    val dateThisMonth = currentYearMonth.atDay(safeDayThisMonth)

    return if (!currentDate.isAfter(dateThisMonth)) {
        dateThisMonth
    } else {
        val nextYearMonth = currentYearMonth.plusMonths(1)
        val safeDayNextMonth = minOf(clampedDay, nextYearMonth.lengthOfMonth())
        nextYearMonth.atDay(safeDayNextMonth)
    }
}
