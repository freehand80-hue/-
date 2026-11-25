package com.example.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

class ParkingFeeReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        ParkingReminderScheduler.ensureNotificationChannel(context)
        if (!ParkingReminderScheduler.hasNotificationPermission(context)) {
            return
        }

        val memo = intent.getStringExtra(ParkingReminderScheduler.EXTRA_MEMO).orEmpty().trim()
        val contentText = if (memo.isNotEmpty()) {
            "$memo 위치에 주차한 지 2시간이 지났습니다. 주차 요금 정산을 잊지 마세요!"
        } else {
            "주차 위치를 저장한 지 2시간이 지났습니다. 주차 요금 정산을 잊지 마세요!"
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            ParkingReminderScheduler.NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ParkingReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_car)
            .setContentTitle(context.getString(R.string.notification_fee_title))
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context)
                .notify(ParkingReminderScheduler.NOTIFICATION_ID, notification)
        }
    }
}
