package dev.whitefire.nit.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.whitefire.nit.domain.model.WorkTimeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

/**
 * Repository for user preferences using DataStore
 */
class UserPreferencesRepository private constructor(
    private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nit_prefs")
    private val dataStore = context.dataStore
    
    companion object {
        // Preference keys
        private val WEEKLY_TARGET_HOURS = floatPreferencesKey("weekly_target_hours")
        private val MON_START_HOUR = intPreferencesKey("mon_start_hour")
        private val MON_START_MINUTE = intPreferencesKey("mon_start_minute")
        private val MON_END_HOUR = intPreferencesKey("mon_end_hour")
        private val MON_END_MINUTE = intPreferencesKey("mon_end_minute")
        private val TUE_START_HOUR = intPreferencesKey("tue_start_hour")
        private val TUE_START_MINUTE = intPreferencesKey("tue_start_minute")
        private val TUE_END_HOUR = intPreferencesKey("tue_end_hour")
        private val TUE_END_MINUTE = intPreferencesKey("tue_end_minute")
        private val WED_START_HOUR = intPreferencesKey("wed_start_hour")
        private val WED_START_MINUTE = intPreferencesKey("wed_start_minute")
        private val WED_END_HOUR = intPreferencesKey("wed_end_hour")
        private val WED_END_MINUTE = intPreferencesKey("wed_end_minute")
        private val THU_START_HOUR = intPreferencesKey("thu_start_hour")
        private val THU_START_MINUTE = intPreferencesKey("thu_start_minute")
        private val THU_END_HOUR = intPreferencesKey("thu_end_hour")
        private val THU_END_MINUTE = intPreferencesKey("thu_end_minute")
        private val FRI_START_HOUR = intPreferencesKey("fri_start_hour")
        private val FRI_START_MINUTE = intPreferencesKey("fri_start_minute")
        private val FRI_END_HOUR = intPreferencesKey("fri_end_hour")
        private val FRI_END_MINUTE = intPreferencesKey("fri_end_minute")
        private val BREAK_AFTER_HOURS = floatPreferencesKey("break_after_hours")
        private val BREAK_DURATION_HOURS = floatPreferencesKey("break_duration_hours")
        private val SHOW_BREAK_WARNING = booleanPreferencesKey("show_break_warning")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val AUTO_CALCULATE_BREAK = booleanPreferencesKey("auto_calculate_break")
        private val ENABLE_KERNZEITEN = booleanPreferencesKey("enable_kernzeiten")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val FRIDAY_TARGET_MODE = stringPreferencesKey("friday_target_mode")
        private val CUSTOM_FRIDAY_TARGET_HOURS = floatPreferencesKey("custom_friday_target_hours")
        
        @Volatile
        private var instance: UserPreferencesRepository? = null
        
        fun getInstance(context: Context): UserPreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: UserPreferencesRepository(context).also { instance = it }
            }
        }
    }
    
    // Work time configuration
    
    val workTimeConfigFlow: Flow<WorkTimeConfig> = dataStore.data
        .map { preferences ->
            val modeStr = preferences[FRIDAY_TARGET_MODE] ?: WorkTimeConfig.FridayExitMode.EARLY_KERNZEIT.name
            val fridayMode = try {
                WorkTimeConfig.FridayExitMode.valueOf(modeStr)
            } catch (e: Exception) {
                WorkTimeConfig.FridayExitMode.EARLY_KERNZEIT
            }

            WorkTimeConfig(
                weeklyTargetHours = preferences[WEEKLY_TARGET_HOURS] ?: 38.5f,
                enableKernzeiten = preferences[ENABLE_KERNZEITEN] ?: true,
                fridayTargetMode = fridayMode,
                customFridayTargetHours = preferences[CUSTOM_FRIDAY_TARGET_HOURS] ?: 3.0f,
                coreTimes = mapOf(
                    java.time.DayOfWeek.MONDAY to WorkTimeConfig.CoreTime(
                        LocalTime.of(
                            preferences[MON_START_HOUR] ?: 9,
                            preferences[MON_START_MINUTE] ?: 30
                        ),
                        LocalTime.of(
                            preferences[MON_END_HOUR] ?: 16,
                            preferences[MON_END_MINUTE] ?: 0
                        )
                    ),
                    java.time.DayOfWeek.TUESDAY to WorkTimeConfig.CoreTime(
                        LocalTime.of(
                            preferences[TUE_START_HOUR] ?: 9,
                            preferences[TUE_START_MINUTE] ?: 30
                        ),
                        LocalTime.of(
                            preferences[TUE_END_HOUR] ?: 16,
                            preferences[TUE_END_MINUTE] ?: 0
                        )
                    ),
                    java.time.DayOfWeek.WEDNESDAY to WorkTimeConfig.CoreTime(
                        LocalTime.of(
                            preferences[WED_START_HOUR] ?: 9,
                            preferences[WED_START_MINUTE] ?: 30
                        ),
                        LocalTime.of(
                            preferences[WED_END_HOUR] ?: 16,
                            preferences[WED_END_MINUTE] ?: 0
                        )
                    ),
                    java.time.DayOfWeek.THURSDAY to WorkTimeConfig.CoreTime(
                        LocalTime.of(
                            preferences[THU_START_HOUR] ?: 9,
                            preferences[THU_START_MINUTE] ?: 30
                        ),
                        LocalTime.of(
                            preferences[THU_END_HOUR] ?: 16,
                            preferences[THU_END_MINUTE] ?: 0
                        )
                    ),
                    java.time.DayOfWeek.FRIDAY to WorkTimeConfig.CoreTime(
                        LocalTime.of(
                            preferences[FRI_START_HOUR] ?: 9,
                            preferences[FRI_START_MINUTE] ?: 30
                        ),
                        LocalTime.of(
                            preferences[FRI_END_HOUR] ?: 12,
                            preferences[FRI_END_MINUTE] ?: 30
                        )
                    ),
                    java.time.DayOfWeek.SATURDAY to WorkTimeConfig.CoreTime(null, null),
                    java.time.DayOfWeek.SUNDAY to WorkTimeConfig.CoreTime(null, null)
                ),
                breakRules = listOf(
                    WorkTimeConfig.BreakRule(
                        afterHours = preferences[BREAK_AFTER_HOURS] ?: 6f,
                        durationHours = preferences[BREAK_DURATION_HOURS] ?: 0.5f
                    )
                )
            )
        }
    
    suspend fun setWorkTimeConfig(config: WorkTimeConfig) {
        dataStore.edit { preferences ->
            preferences[WEEKLY_TARGET_HOURS] = config.weeklyTargetHours
            preferences[ENABLE_KERNZEITEN] = config.enableKernzeiten
            preferences[FRIDAY_TARGET_MODE] = config.fridayTargetMode.name
            preferences[CUSTOM_FRIDAY_TARGET_HOURS] = config.customFridayTargetHours
            
            config.coreTimes[java.time.DayOfWeek.MONDAY]?.let { core ->
                preferences[MON_START_HOUR] = core.start?.hour ?: 9
                preferences[MON_START_MINUTE] = core.start?.minute ?: 30
                preferences[MON_END_HOUR] = core.end?.hour ?: 16
                preferences[MON_END_MINUTE] = core.end?.minute ?: 0
            }
            
            config.coreTimes[java.time.DayOfWeek.TUESDAY]?.let { core ->
                preferences[TUE_START_HOUR] = core.start?.hour ?: 9
                preferences[TUE_START_MINUTE] = core.start?.minute ?: 30
                preferences[TUE_END_HOUR] = core.end?.hour ?: 16
                preferences[TUE_END_MINUTE] = core.end?.minute ?: 0
            }
            
            config.coreTimes[java.time.DayOfWeek.WEDNESDAY]?.let { core ->
                preferences[WED_START_HOUR] = core.start?.hour ?: 9
                preferences[WED_START_MINUTE] = core.start?.minute ?: 30
                preferences[WED_END_HOUR] = core.end?.hour ?: 16
                preferences[WED_END_MINUTE] = core.end?.minute ?: 0
            }
            
            config.coreTimes[java.time.DayOfWeek.THURSDAY]?.let { core ->
                preferences[THU_START_HOUR] = core.start?.hour ?: 9
                preferences[THU_START_MINUTE] = core.start?.minute ?: 30
                preferences[THU_END_HOUR] = core.end?.hour ?: 16
                preferences[THU_END_MINUTE] = core.end?.minute ?: 0
            }
            
            config.coreTimes[java.time.DayOfWeek.FRIDAY]?.let { core ->
                preferences[FRI_START_HOUR] = core.start?.hour ?: 9
                preferences[FRI_START_MINUTE] = core.start?.minute ?: 30
                preferences[FRI_END_HOUR] = core.end?.hour ?: 12
                preferences[FRI_END_MINUTE] = core.end?.minute ?: 30
            }
            
            if (config.breakRules.isNotEmpty()) {
                preferences[BREAK_AFTER_HOURS] = config.breakRules[0].afterHours
                preferences[BREAK_DURATION_HOURS] = config.breakRules[0].durationHours
            }
        }
    }
    
    // UI Preferences

    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    val showBreakWarningFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[SHOW_BREAK_WARNING] ?: true }
    
    suspend fun setShowBreakWarning(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_BREAK_WARNING] = show
        }
    }
    
    val darkModeFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[DARK_MODE] ?: false }
    
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }
    
    val autoCalculateBreakFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[AUTO_CALCULATE_BREAK] ?: true }
    
    suspend fun setAutoCalculateBreak(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_CALCULATE_BREAK] = enabled
        }
    }
    
    /**
     * Reset all preferences to defaults
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
