package com.example.cardmanager.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cardmanager.MainActivity
import com.example.cardmanager.R
import com.example.cardmanager.core.database.entity.CreditCardEntity

object NotificationHelper {

    const val CHANNEL_ID = "vaultcard_repayment_reminders"
    private const val CHANNEL_NAME = "VaultCard Reminders"
    private const val CHANNEL_DESC = "Notifications for upcoming credit card repayment deadlines"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showRepaymentReminder(
        context: Context,
        card: CreditCardEntity,
        daysRemaining: Long
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            card.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (daysRemaining == 0L) {
            "Bill Due Today: ${card.cardAlias}"
        } else {
            "Payment Due in $daysRemaining Days: ${card.cardAlias}"
        }

        val message = "${card.bankName} (•••• ${card.last4Digits}) repayment day is on the ${card.repaymentDay}th."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(card.id.toInt() + 1000, notification)
        } catch (_: SecurityException) {
            // Android 13+ permission might not be granted yet
        }
    }
}
