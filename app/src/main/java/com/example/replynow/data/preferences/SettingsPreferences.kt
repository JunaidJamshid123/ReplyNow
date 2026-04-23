package com.example.replynow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_DELAY_MILLIS = longPreferencesKey("reminder_delay_millis")

        const val DEFAULT_DELAY_MILLIS = 3_600_000L // 1 hour
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    val reminderDelayMillis: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_DELAY_MILLIS] ?: DEFAULT_DELAY_MILLIS
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setReminderDelayMillis(millis: Long) {
        context.dataStore.edit { it[REMINDER_DELAY_MILLIS] = millis }
    }
}
