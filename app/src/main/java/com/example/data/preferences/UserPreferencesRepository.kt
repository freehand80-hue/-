package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class MapAppPreference(val key: String) {
    AUTO("auto"),
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google");

    companion object {
        fun fromKey(key: String?): MapAppPreference {
            return entries.firstOrNull { it.key == key } ?: AUTO
        }
    }
}

data class FeeReminderState(
    val enabled: Boolean = false,
    val spotId: Long = -1L,
    val triggerAtMillis: Long = 0L
)

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "parking_user_preferences"
)

class UserPreferencesRepository(private val context: Context) {
    private val mapAppKey = stringPreferencesKey("preferred_map_app")
    private val reminderEnabledKey = booleanPreferencesKey("fee_reminder_enabled")
    private val reminderSpotIdKey = longPreferencesKey("fee_reminder_spot_id")
    private val reminderTriggerAtKey = longPreferencesKey("fee_reminder_trigger_at")

    val preferredMapApp: Flow<MapAppPreference> = context.userPreferencesDataStore.data
        .map { prefs -> MapAppPreference.fromKey(prefs[mapAppKey]) }

    val feeReminderState: Flow<FeeReminderState> = context.userPreferencesDataStore.data
        .map { prefs ->
            FeeReminderState(
                enabled = prefs[reminderEnabledKey] ?: false,
                spotId = prefs[reminderSpotIdKey] ?: -1L,
                triggerAtMillis = prefs[reminderTriggerAtKey] ?: 0L
            )
        }

    suspend fun setPreferredMapApp(preference: MapAppPreference) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[mapAppKey] = preference.key
        }
    }

    suspend fun setFeeReminder(enabled: Boolean, spotId: Long, triggerAtMillis: Long) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[reminderEnabledKey] = enabled
            prefs[reminderSpotIdKey] = spotId
            prefs[reminderTriggerAtKey] = triggerAtMillis
        }
    }

    suspend fun clearFeeReminder() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[reminderEnabledKey] = false
            prefs[reminderSpotIdKey] = -1L
            prefs[reminderTriggerAtKey] = 0L
        }
    }
}
