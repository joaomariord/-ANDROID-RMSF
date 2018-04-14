package com.joaomariodev.rmsfsensoractuationapp.Utilities

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class SharedPrefs(context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var apiServer : String
        get() = prefs.getString("API_SERVER","http://rmsf-server.herokuapp.com")
        set(value) { Log.d("SET", "SET NOT ALLOWED") }

    var apiPort : String
        get() = prefs.getString("API_PORT","80")
        set(value) { Log.d("SET", "SET NOT ALLOWED") }

    var StoredFCMtoken : String
        get() = prefs.getString("FCM_TOKEN","")
        set(value) = prefs.edit().putString("FCM_TOKEN", value).apply()

    var LoggedIn : Boolean
        get() = prefs.getBoolean("LOGGED_IN", false)
        set(value) = prefs.edit().putBoolean("LOGGED_IN",value).apply()

    var LoginToken : String
        get() = prefs.getString("LOGIN_TOKEN", "")
        set(value) = prefs.edit().putString("LOGIN_TOKEN", value).apply()

    var userEmail : String
        get() = prefs.getString("USER_EMAIL", "")
        set(value) = prefs.edit().putString("USER_EMAIL", value).apply()

    var userName: String
        get() = prefs.getString("USER_NAME", "")
        set(value) = prefs.edit().putString("USER_NAME", value).apply()

    val requestQueue: RequestQueue = Volley.newRequestQueue(context, CustomHurlClass())
}