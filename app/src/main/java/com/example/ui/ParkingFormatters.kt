package com.example.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object ParkingFormatters {
    private val fullTimeFormat = SimpleDateFormat("M월 d일 (E) a h:mm", Locale.KOREAN)
    private val shortTimeFormat = SimpleDateFormat("yyyy.MM.dd a h:mm", Locale.KOREAN)
    private val clockTimeFormat = SimpleDateFormat("a h:mm", Locale.KOREAN)

    fun formatSavedTime(millis: Long): String {
        return fullTimeFormat.format(Date(millis))
    }

    fun formatHistoryTime(millis: Long): String {
        return shortTimeFormat.format(Date(millis))
    }

    fun formatClockTime(millis: Long): String {
        return clockTimeFormat.format(Date(millis))
    }

    fun formatCoordinates(latitude: Double, longitude: Double): String {
        return String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
    }

    fun formatAccuracy(accuracyMeters: Float?): String? {
        if (accuracyMeters == null || accuracyMeters <= 0f) return null
        return "오차 ±${accuracyMeters.toInt()}m"
    }

    fun formatElapsed(savedAtMillis: Long, nowMillis: Long): String {
        val diffSeconds = max(0L, (nowMillis - savedAtMillis) / 1000L)
        val totalMinutes = diffSeconds / 60L
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            totalMinutes < 1L -> "방금 주차함"
            hours == 0L -> "${minutes}분 경과"
            minutes == 0L -> "${hours}시간 경과"
            else -> "${hours}시간 ${minutes}분 경과"
        }
    }

    fun formatRemaining(targetMillis: Long, nowMillis: Long): String {
        val diffSeconds = max(0L, (targetMillis - nowMillis) / 1000L)
        val totalMinutes = (diffSeconds + 59L) / 60L
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            diffSeconds <= 0L -> "알림 시각 도달"
            hours == 0L -> "${minutes}분 남음"
            minutes == 0L -> "${hours}시간 남음"
            else -> "${hours}시간 ${minutes}분 남음"
        }
    }
}
