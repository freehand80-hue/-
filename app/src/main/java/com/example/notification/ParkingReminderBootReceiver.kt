package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.ParkingDatabase
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ParkingReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val prefs = UserPreferencesRepository(appContext)
                val reminderState = prefs.feeReminderState.first()
                if (!reminderState.enabled || reminderState.triggerAtMillis <= System.currentTimeMillis()) {
                    return@launch
                }
                val activeSpot = ParkingDatabase.getInstance(appContext)
                    .parkingSpotDao()
                    .observeActiveSpot()
                    .first() ?: return@launch

                if (activeSpot.id == reminderState.spotId) {
                    ParkingReminderScheduler.scheduleTwoHourReminder(
                        context = appContext,
                        spotId = activeSpot.id,
                        memo = activeSpot.floorZoneMemo,
                        savedAtMillis = activeSpot.savedAtMillis,
                        triggerAtMillis = reminderState.triggerAtMillis
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
