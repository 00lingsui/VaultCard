package com.example.cardmanager.core.common.util

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import com.example.cardmanager.core.database.entity.CreditCardEntity

object AlarmIntentHelper {

    fun setRepaymentAlarm(context: Context, card: CreditCardEntity, hour: Int = 9, minute: Int = 0) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "还款提醒: ${card.bankName} (${card.cardAlias})")
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "未找到系统时钟/闹钟应用", Toast.LENGTH_SHORT).show()
        }
    }
}
