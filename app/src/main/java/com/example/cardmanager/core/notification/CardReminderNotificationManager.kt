package com.example.cardmanager.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cardmanager.MainActivity
import com.example.cardmanager.R
import com.example.cardmanager.core.currency.AppCurrency
import com.example.cardmanager.core.database.entity.CreditCardEntity

object CardReminderNotificationManager {

    const val CHANNEL_ID = "vaultcard_repayment_reminders"
    private const val TEST_NOTIFICATION_ID = 90_001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return runtimeGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun hasRuntimePermissionOnly(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showPaymentDueNotification(
        context: Context,
        card: CreditCardEntity,
        daysRemaining: Long,
        currentBalance: Double
    ): Boolean {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return false

        val currency = AppCurrency.fromCode(card.currencyCode)
        val formattedBalance = currency.format(currentBalance)
        val cardTitleName = card.cardAlias.ifBlank { card.bankName }

        val urgencyText = when (daysRemaining) {
            0L -> context.getString(R.string.notif_due_today_label)
            1L -> context.getString(R.string.notif_due_tomorrow_label)
            else -> context.getString(R.string.notif_due_in_days_label, daysRemaining.toInt())
        }

        val title = context.getString(
            R.string.notif_payment_due_title,
            urgencyText,
            cardTitleName
        )
        val body = context.getString(
            R.string.notif_payment_due_body,
            card.bankName,
            card.last4Digits,
            urgencyText,
            formattedBalance
        )

        val notificationId = ((card.id.toInt() and 0xFFFF) * 100) + 10 + daysRemaining.toInt().coerceIn(0, 31)
        return postNotification(
            context = context,
            notificationId = notificationId,
            requestCode = notificationId,
            title = title,
            body = body
        )
    }

    fun showStatementIssuedNotification(
        context: Context,
        card: CreditCardEntity,
        currentBalance: Double
    ): Boolean {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return false

        val currency = AppCurrency.fromCode(card.currencyCode)
        val formattedBalance = currency.format(currentBalance)
        val cardTitleName = card.cardAlias.ifBlank { card.bankName }

        val title = context.getString(
            R.string.notif_statement_issued_title,
            cardTitleName
        )
        val body = context.getString(
            R.string.notif_statement_issued_body,
            card.bankName,
            card.last4Digits,
            formattedBalance
        )

        val notificationId = ((card.id.toInt() and 0xFFFF) * 100) + 90
        return postNotification(
            context = context,
            notificationId = notificationId,
            requestCode = notificationId,
            title = title,
            body = body
        )
    }

    fun showTestNotification(context: Context): Boolean {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return false

        val sampleAmount = AppCurrency.CNY.format(2580.0)
        val title = context.getString(R.string.notif_test_title)
        val body = context.getString(R.string.notif_test_body, sampleAmount)

        return postNotification(
            context = context,
            notificationId = TEST_NOTIFICATION_ID,
            requestCode = TEST_NOTIFICATION_ID,
            title = title,
            body = body
        )
    }

    @SuppressLint("MissingPermission")
    private fun postNotification(
        context: Context,
        notificationId: Int,
        requestCode: Int,
        title: String,
        body: String
    ): Boolean {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
