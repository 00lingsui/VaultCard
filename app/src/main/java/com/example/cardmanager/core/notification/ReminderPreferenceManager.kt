package com.example.cardmanager.core.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vaultcard_reminder_prefs"
)

data class ReminderSettings(
    val enabled: Boolean = true,
    val advanceDays: Set<Int> = setOf(3, 1),
    val notifyOnDueDate: Boolean = true,
    val notifyOnStatementDay: Boolean = true,
    val reminderHour: Int = DEFAULT_REMINDER_HOUR
) {
    companion object {
        const val DEFAULT_REMINDER_HOUR = 9
        val ALLOWED_ADVANCE_DAYS = listOf(1, 3, 5)
        val ALLOWED_REMINDER_HOURS = listOf(9, 12, 18, 20)
    }
}

@Singleton
class ReminderPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _settingsFlow = MutableStateFlow(ReminderSettings())
    val settingsFlow: StateFlow<ReminderSettings> = _settingsFlow.asStateFlow()

    init {
        scope.launch {
            context.reminderDataStore.data
                .catch { exception ->
                    if (exception is IOException) emit(emptyPreferences()) else throw exception
                }
                .map(::parseSettings)
                .collect { settings ->
                    _settingsFlow.value = settings
                }
        }
    }

    suspend fun getSettings(): ReminderSettings {
        val prefs = context.reminderDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()
        return parseSettings(prefs)
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[KEY_ENABLED] = enabled
        }
    }

    suspend fun toggleAdvanceDay(day: Int) {
        if (day !in ReminderSettings.ALLOWED_ADVANCE_DAYS) return
        context.reminderDataStore.edit { prefs ->
            val current = parseAdvanceDays(prefs[KEY_ADVANCE_DAYS])
            val updated = if (day in current) current - day else current + day
            prefs[KEY_ADVANCE_DAYS] = updated.sorted().joinToString(",")
        }
    }

    suspend fun setNotifyOnDueDate(enabled: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[KEY_NOTIFY_DUE_DATE] = enabled
        }
    }

    suspend fun setNotifyOnStatementDay(enabled: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[KEY_NOTIFY_STATEMENT_DAY] = enabled
        }
    }

    suspend fun setReminderHour(hour: Int) {
        val validHour = if (hour in ReminderSettings.ALLOWED_REMINDER_HOURS) {
            hour
        } else {
            ReminderSettings.DEFAULT_REMINDER_HOUR
        }
        context.reminderDataStore.edit { prefs ->
            prefs[KEY_REMINDER_HOUR] = validHour
        }
    }

    suspend fun hasNotifiedOnDate(
        cardId: Long,
        eventKey: String,
        date: LocalDate = LocalDate.now()
    ): Boolean {
        val prefs = context.reminderDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()
        val entry = buildDedupEntry(cardId, eventKey, date)
        val entries = parseDedupEntries(prefs[KEY_NOTIFIED_LOG])
        return entry in entries
    }

    suspend fun markNotifiedOnDate(
        cardId: Long,
        eventKey: String,
        date: LocalDate = LocalDate.now()
    ) {
        val cutoffDate = date.minusDays(DEDUP_RETENTION_DAYS)
        val newEntry = buildDedupEntry(cardId, eventKey, date)
        context.reminderDataStore.edit { prefs ->
            val retained = parseDedupEntries(prefs[KEY_NOTIFIED_LOG])
                .filter { raw ->
                    val datePart = raw.substringAfterLast(':', "")
                    val parsedDate = runCatching { LocalDate.parse(datePart) }.getOrNull()
                    parsedDate != null && !parsedDate.isBefore(cutoffDate)
                }
                .toMutableSet()
            retained.add(newEntry)
            prefs[KEY_NOTIFIED_LOG] = retained.joinToString("|")
        }
    }

    private fun parseSettings(prefs: Preferences): ReminderSettings {
        val enabled = prefs[KEY_ENABLED] ?: true
        val advanceDays = if (prefs.contains(KEY_ADVANCE_DAYS)) {
            parseAdvanceDays(prefs[KEY_ADVANCE_DAYS])
        } else {
            setOf(3, 1)
        }
        val notifyDue = prefs[KEY_NOTIFY_DUE_DATE] ?: true
        val notifyStatement = prefs[KEY_NOTIFY_STATEMENT_DAY] ?: true
        val hour = (prefs[KEY_REMINDER_HOUR] ?: ReminderSettings.DEFAULT_REMINDER_HOUR).let {
            if (it in ReminderSettings.ALLOWED_REMINDER_HOURS) it else ReminderSettings.DEFAULT_REMINDER_HOUR
        }
        return ReminderSettings(
            enabled = enabled,
            advanceDays = advanceDays,
            notifyOnDueDate = notifyDue,
            notifyOnStatementDay = notifyStatement,
            reminderHour = hour
        )
    }

    private fun parseAdvanceDays(raw: String?): Set<Int> {
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in ReminderSettings.ALLOWED_ADVANCE_DAYS }
            .toSet()
    }

    private fun parseDedupEntries(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split("|").filter { it.isNotBlank() }.toSet()
    }

    private fun buildDedupEntry(cardId: Long, eventKey: String, date: LocalDate): String =
        "$cardId:$eventKey:$date"

    companion object {
        private const val DEDUP_RETENTION_DAYS = 14L
        private val KEY_ENABLED = booleanPreferencesKey("reminders_enabled")
        private val KEY_ADVANCE_DAYS = stringPreferencesKey("advance_notice_days")
        private val KEY_NOTIFY_DUE_DATE = booleanPreferencesKey("notify_on_due_date")
        private val KEY_NOTIFY_STATEMENT_DAY = booleanPreferencesKey("notify_on_statement_day")
        private val KEY_REMINDER_HOUR = intPreferencesKey("preferred_reminder_hour")
        private val KEY_NOTIFIED_LOG = stringPreferencesKey("notified_events_log")
    }
}
