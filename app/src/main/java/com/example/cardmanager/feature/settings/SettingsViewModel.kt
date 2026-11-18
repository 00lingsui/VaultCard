package com.example.cardmanager.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardmanager.core.notification.CardReminderNotificationManager
import com.example.cardmanager.core.notification.ReminderPreferenceManager
import com.example.cardmanager.core.notification.ReminderScheduler
import com.example.cardmanager.core.notification.ReminderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val preferenceManager: ReminderPreferenceManager,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val reminderSettings: StateFlow<ReminderSettings> = preferenceManager.settingsFlow

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setEnabled(enabled)
            val currentHour = preferenceManager.getSettings().reminderHour
            reminderScheduler.syncSchedule(reminderHour = currentHour, enabled = enabled)
            if (enabled) {
                reminderScheduler.triggerImmediateCheck()
            }
        }
    }

    fun toggleAdvanceDay(day: Int) {
        viewModelScope.launch {
            preferenceManager.toggleAdvanceDay(day)
            val settings = preferenceManager.getSettings()
            if (settings.enabled) {
                reminderScheduler.triggerImmediateCheck()
            }
        }
    }

    fun setNotifyOnDueDate(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setNotifyOnDueDate(enabled)
            if (enabled && preferenceManager.getSettings().enabled) {
                reminderScheduler.triggerImmediateCheck()
            }
        }
    }

    fun setNotifyOnStatementDay(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setNotifyOnStatementDay(enabled)
            if (enabled && preferenceManager.getSettings().enabled) {
                reminderScheduler.triggerImmediateCheck()
            }
        }
    }

    fun setReminderHour(hour: Int) {
        viewModelScope.launch {
            preferenceManager.setReminderHour(hour)
            val settings = preferenceManager.getSettings()
            reminderScheduler.syncSchedule(reminderHour = hour, enabled = settings.enabled)
        }
    }

    fun sendTestNotification(): Boolean {
        return CardReminderNotificationManager.showTestNotification(appContext)
    }
}
