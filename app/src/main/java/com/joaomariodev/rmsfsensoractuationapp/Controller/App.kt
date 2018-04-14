package com.joaomariodev.rmsfsensoractuationapp.Controller

import android.app.Application
import com.joaomariodev.rmsfsensoractuationapp.Utilities.SharedPrefs

class App : Application() {
    companion object {
        lateinit var prefs: SharedPrefs

        fun isMainActivityVisible(): Boolean {
            return activityVisible
        }

        fun mainActivityResumed() {
            activityVisible = true
        }

        fun mainActivityPaused() {
            activityVisible = false
        }

        private var activityVisible: Boolean = false
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }

}