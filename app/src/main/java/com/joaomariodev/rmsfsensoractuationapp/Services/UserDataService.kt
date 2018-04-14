package com.joaomariodev.rmsfsensoractuationapp.Services

import android.util.Log
import com.joaomariodev.rmsfsensoractuationapp.Controller.App
import com.joaomariodev.rmsfsensoractuationapp.Model.TTNApplication
import com.joaomariodev.rmsfsensoractuationapp.Model.TTNDevice
import org.json.JSONException
import org.json.JSONObject


object UserDataService {
    var email = ""
        private set
    var name = ""
        private set
    var appsList = ArrayList<TTNApplication>()
        private set
    var selectedAppID: String? = null
        private set
    var selectedDeviceID: String? = null
        private set

    fun getLoginFromPrefs(){
        this.email = App.prefs.userEmail
        this.name = App.prefs.userName
    }

    fun login(email: String, name : String, token : String){
        App.prefs.userEmail = email
        this.email = email

        App.prefs.userName = name
        this.name = name

        App.prefs.LoginToken = token

        App.prefs.LoggedIn = true

        clearSelectedDevice()
    }

    //Made to receive apps list from get /status
    fun fillAppsList (webList : JSONObject){
        appsList.clear()
        try {
            val applications = webList.getJSONArray("applications")
            for (i in 0 until applications.length()){
                val app = applications[i] as JSONObject
                //Create app
                val appID = app.getString("appID")
                val appKey = app.getString("appKey")
                val newApp = TTNApplication(appKey, appID)
                //Run for each device in app
                val devices = app.getJSONArray("devices")
                for (j in 0 until devices.length()){
                    val device = devices[j] as JSONObject
                    //Create device
                    val deviceID = device.getString("deviceID")
                    newApp.devicesList.add(TTNDevice(deviceID))

                    //Try to fill device if info is available
                    try {
                        val deviceStatus = device.getJSONObject("deviceStatus")
                        //Device has status:
                            //TODO:Start filling our device
                    } catch (e: JSONException) {
                        Log.d("UserDataService_FillDev", "No info to fill: " + e.toString())
                    }
                }

                appsList.add(newApp)
            }
        }
        catch (e: JSONException){
            Log.d("UserDataService_fillApp", "Cannot fill apps: "+ e.toString())
        }

    }

    fun logout(){
        email = ""
        name = ""
        //CLEAR APPS AND DEVICES LIST
        appsList.clear()
        App.prefs.LoginToken = ""
        App.prefs.userEmail = ""
        App.prefs.userName = ""
        App.prefs.LoggedIn = false

        clearSelectedDevice()
        //TODO: CLEAR FRAGMENTS DISPLAY DATA
    }

    fun clearSelectedDevice() {
        selectedAppID = null
        selectedDeviceID = null
    }

    fun setSelectedDevice(appID: String, deviceID: String){
        selectedAppID = appID
        selectedDeviceID = deviceID
    }

    fun isDeviceSelected(): Boolean{
        return !(selectedAppID == null && selectedDeviceID == null)
    }
}