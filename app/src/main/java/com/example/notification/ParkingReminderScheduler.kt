package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat

object ParkingReminderScheduler {
    const val CHANNEL_ID = "parking_fee_reminder_channel"
    const val NOTIFICATION_ID = 2001
    private const val ALARM_REQUEST_CODE = 2002
    const val TWO_HOURS_MILLIS = 2L * 60L * 60L * 1000L

    const val EXTRA_SPOT_ID = "extra_spot_id"
    const val EXTRA_MEMO = "extra_memo"
    const val EXTRA_SAVED_AT = "extra_saved_at"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "주차 요금 정산 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "주차 위치 저장 후 2시간이 지나면 주차 요금 정산을 알려드립니다."
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun scheduleTwoHourReminder(
        context: Context,
        spotId: Long,
        memo: String,
        savedAtMillis: Long,
        triggerAtMillis: Long = savedAtMillis + TWO_HOURS_MILLIS
    ): Long {
        ensureNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildAlarmPendingIntent(
            context = context,
            spotId = spotId,
            memo = memo,
            savedAtMillis = savedAtMillis
        )
        alarmManager.cancel(pendingIntent)

        val safeTrigger = triggerAtMillis.coerceAtLeast(System.currentTimeMillis() + 5_000L)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            safeTrigger,
            pendingIntent
        )
        return safeTrigger
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildAlarmPendingIntent(
            context = context,
            spotId = -1L,
            memo = "",
            savedAtMillis = 0L
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun buildAlarmPendingIntent(
        context: Context,
        spotId: Long,
        memo: String,
        savedAtMillis: Long
    ): PendingIntent {
        val intent = Intent(context, ParkingFeeReminderReceiver::class.java).apply {
            putExtra(EXTRA_SPOT_ID, spotId)
            putExtra(EXTRA_MEMO, memo)
            putExtra(EXTRA_SAVED_AT, savedAtMillis)
        }
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
