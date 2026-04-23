package com.example.replynow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.replynow.data.preferences.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val reminderDelayMillis: Long = 3_600_000L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: SettingsPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.notificationsEnabled,
        preferences.reminderDelayMillis
    ) { enabled, delay ->
        SettingsUiState(notificationsEnabled = enabled, reminderDelayMillis = delay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }

    fun setReminderDelay(millis: Long) {
        viewModelScope.launch { preferences.setReminderDelayMillis(millis) }
    }
}
