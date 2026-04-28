package com.example.floodwatch

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.floodwatch.settings.SettingsManager

class FloodWatchApp : Application() {

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsManager = SettingsManager(this)
        applyTheme()
    }

    private fun applyTheme() {
        val mode = when (settingsManager.themeMode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    companion object {
        lateinit var instance: FloodWatchApp
            private set
    }
}
