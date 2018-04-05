package com.joaomariodev.rmsfsensoractuationapp.Utilities

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.toolbox.Volley

class SharedPrefs(context: Context) {
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var apiServer : String
        get() = prefs.getString("API_SERVER","http://rmsf-server.herokuapp.com")
        set(value) { Log.d("SET", "SET NOT ALLOWED") }

    var apiPort : String
        get() = prefs.getString("API_PORT","80")
        set(value) { Log.d("SET", "SET NOT ALLOWED") }

    var backgroundSyncPeriod : String
        get() {
            return prefs.getString("sync_frequency", "-1")

        }
        set(value) { Log.d("SET", "SET NOT ALLOWED") }

    val requestQueue = Volley.newRequestQueue(context)
}