package com.example.cardmanager.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cardmanager.core.common.util.calculateDaysUntilRepayment
import com.example.cardmanager.core.common.util.calculateDaysUntilStatement
import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.dao.TransactionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class RepaymentReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val creditCardDao: CreditCardDao,
    private val transactionDao: TransactionDao,
    private val preferenceManager: ReminderPreferenceManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settings = preferenceManager.getSettings()
            if (!settings.enabled) {
                return Result.success()
            }

            if (!CardReminderNotificationManager.hasNotificationPermission(applicationContext)) {
                return Result.success()
            }

            val cards = creditCardDao.getAllCardsList()
            if (cards.isEmpty()) {
                return Result.success()
            }

            val transactions = transactionDao.getAllTransactionsList()
            val debtByCardId = transactions
                .groupBy { it.cardId }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            val today = LocalDate.now()

            for (card in cards) {
                val currentBalance = debtByCardId[card.id] ?: 0.0
                val daysUntilDue = card.calculateDaysUntilRepayment(today)

                val shouldNotifyDue = (daysUntilDue == 0L && settings.notifyOnDueDate) ||
                    (daysUntilDue > 0L && daysUntilDue.toInt() in settings.advanceDays)

                if (shouldNotifyDue) {
                    val eventKey = "due_$daysUntilDue"
                    if (!preferenceManager.hasNotifiedOnDate(card.id, eventKey, today)) {
                        val posted = CardReminderNotificationManager.showPaymentDueNotification(
                            context = applicationContext,
                            card = card,
                            daysRemaining = daysUntilDue,
                            currentBalance = currentBalance
                        )
                        if (posted) {
                            preferenceManager.markNotifiedOnDate(card.id, eventKey, today)
                        }
                    }
                }

                if (settings.notifyOnStatementDay) {
                    val daysUntilStatement = card.calculateDaysUntilStatement(today)
                    if (daysUntilStatement == 0L) {
                        val eventKey = "statement_0"
                        if (!preferenceManager.hasNotifiedOnDate(card.id, eventKey, today)) {
                            val posted = CardReminderNotificationManager.showStatementIssuedNotification(
                                context = applicationContext,
                                card = card,
                                currentBalance = currentBalance
                            )
                            if (posted) {
                                preferenceManager.markNotifiedOnDate(card.id, eventKey, today)
                            }
                        }
                    }
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
