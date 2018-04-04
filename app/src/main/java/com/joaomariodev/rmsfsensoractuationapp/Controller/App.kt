package com.joaomariodev.rmsfsensoractuationapp.Controller

import android.app.Application
import com.joaomariodev.rmsfsensoractuationapp.Utilities.SharedPrefs

/**
 * Created by joaom on 04/04/2018.
 */
class App : Application() {
    companion object {
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}