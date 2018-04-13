package com.joaomariodev.rmsfsensoractuationapp.Services

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.joaomariodev.rmsfsensoractuationapp.Controller.App
import org.json.JSONException


object AuthService {

    fun registerUser(email:String, password: String, name:String,complete: (Boolean) -> Unit){
        CloudApi.postRegister(name,email,password, {_ ->
            complete(true)
        }, { error ->
            val errorBody = String(error.networkResponse.data,Charsets.UTF_8)
            Log.d("AuthService", "Could not register user: $errorBody")
            complete(false)
        })
    }

    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit){

        val tokenPush = FirebaseInstanceId.getInstance().token
        App.prefs.StoredFCMtoken = tokenPush ?: ""

        CloudApi.postLogin(email,password, tokenPush, { response ->
            try {
                val xtoken = response.getString("x-auth")
                val xemail = response.getString("email")
                val xname  = response.getString("name")
                UserDataService.login(xemail,xname,xtoken)

                complete(true)
            } catch (exc : JSONException) {
                Log.d("AuthServiceJSON", "JSONexc: " + exc.localizedMessage)
                complete(false)
            }
        }, { error ->
            Log.d("AuthService", "Could not login user:")
            error.printStackTrace()
            complete(false)
        })
    }

    //Done: ADD LOGOUT FUN
    fun logout(complete: (Boolean) -> Unit){
        CloudApi.postLogout(App.prefs.StoredFCMtoken,{ _ ->
            UserDataService.logout()
            complete(true)
        }, {error ->
            Log.d("Error", "Could not logout: $error")
            complete(false)
        })
    }

    //Done: ADD PUSH TOKEN REFRESH FUN
    fun refreshFCMtoken(newToken: String, complete: (Boolean) -> Unit){
        CloudApi.postStoreNewPushToken(newToken,App.prefs.StoredFCMtoken,{ _ ->
            App.prefs.StoredFCMtoken = newToken
            complete(true)
        },{error ->
            Log.d("Error", "Could not logout: $error")
            complete(false)
        })
    }

}