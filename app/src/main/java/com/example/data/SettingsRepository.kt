package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode { SYSTEM, LIGHT, DARK }
enum class AppFontSize { SMALL, MEDIUM, LARGE }

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _fontSize = MutableStateFlow(
        AppFontSize.valueOf(prefs.getString("font_size", AppFontSize.MEDIUM.name) ?: AppFontSize.MEDIUM.name)
    )
    val fontSize: StateFlow<AppFontSize> = _fontSize.asStateFlow()

    private val _treatmentDuration = MutableStateFlow(
        prefs.getInt("treatment_duration", 30)
    )
    val treatmentDuration: StateFlow<Int> = _treatmentDuration.asStateFlow()

    private val _treatmentStartDate = MutableStateFlow(
        prefs.getLong("treatment_start_date", System.currentTimeMillis())
    )
    val treatmentStartDate: StateFlow<Long> = _treatmentStartDate.asStateFlow()

    private val _isAlarmEnabled = MutableStateFlow(
        prefs.getBoolean("is_alarm_enabled", true)
    )
    val isAlarmEnabled: StateFlow<Boolean> = _isAlarmEnabled.asStateFlow()

    init {
        if (!prefs.contains("treatment_start_date")) {
            prefs.edit().putLong("treatment_start_date", System.currentTimeMillis()).apply()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setFontSize(size: AppFontSize) {
        prefs.edit().putString("font_size", size.name).apply()
        _fontSize.value = size
    }
    
    fun setTreatmentDuration(days: Int) {
        prefs.edit().putInt("treatment_duration", days).apply()
        _treatmentDuration.value = days
    }
    
    fun resetTreatmentStartDate() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("treatment_start_date", now).apply()
        _treatmentStartDate.value = now
    }
    
    fun setAlarmEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_alarm_enabled", enabled).apply()
        _isAlarmEnabled.value = enabled
    }
}
