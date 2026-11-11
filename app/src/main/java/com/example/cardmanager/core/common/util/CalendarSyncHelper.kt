package com.example.cardmanager.core.common.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.cardmanager.core.database.entity.CreditCardEntity
import java.time.ZoneId

object CalendarSyncHelper {

    fun addRepaymentCalendarEvent(context: Context, card: CreditCardEntity) {
        val nextDueDate = card.getNextRepaymentDate()
        val startMillis = nextDueDate.atTime(9, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.Events.TITLE, "信用卡还款提醒: ${card.bankName} (${card.cardAlias})")
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "还款截止提醒：${card.cardAlias} (•••• ${card.last4Digits})。额度: ¥${card.creditLimit}"
            )
            putExtra(CalendarContract.Events.RRULE, "FREQ=MONTHLY;BYMONTHDAY=${card.repaymentDay}")
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }
}
