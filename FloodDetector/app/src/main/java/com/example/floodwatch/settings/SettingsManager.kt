package com.example.floodwatch.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_FOLLOW_SYSTEM) ?: THEME_FOLLOW_SYSTEM
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value).apply()
            applyTheme(value)
        }

    var autoSave: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SAVE, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SAVE, value).apply()

    var confidenceThreshold: Int
        get() = prefs.getInt(KEY_CONFIDENCE_THRESHOLD, 50)
        set(value) = prefs.edit().putInt(KEY_CONFIDENCE_THRESHOLD, value).apply()

    var outputQuality: String
        get() = prefs.getString(KEY_OUTPUT_QUALITY, QUALITY_MEDIUM) ?: QUALITY_MEDIUM
        set(value) = prefs.edit().putString(KEY_OUTPUT_QUALITY, value).apply()

    private fun applyTheme(mode: String) {
        val nightMode = when (mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        private const val PREFS_NAME = "flood_watch_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_CONFIDENCE_THRESHOLD = "confidence_threshold"
        private const val KEY_OUTPUT_QUALITY = "output_quality"

        const val THEME_FOLLOW_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val QUALITY_LOW = "low"
        const val QUALITY_MEDIUM = "medium"
        const val QUALITY_HIGH = "high"
    }
}
