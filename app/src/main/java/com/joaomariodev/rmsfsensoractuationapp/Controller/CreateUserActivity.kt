package com.joaomariodev.rmsfsensoractuationapp.Controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.joaomariodev.rmsfsensoractuationapp.R
import com.joaomariodev.rmsfsensoractuationapp.Services.AuthService
import com.joaomariodev.rmsfsensoractuationapp.Utilities.ConstantsO.BROADCAST_USER_LOGGED_IN
import kotlinx.android.synthetic.main.activity_create_user.*


class CreateUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        createSpinner.visibility = View.INVISIBLE
    }


    fun createUserBtnClicked(view: View){
        enableSpinner(true)

        val email = createEmailText.text.toString()
        val password = createPasswordText.text.toString()
        val userName = createUserText.text.toString()

        hideKeyboard()

        if(userName.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()){
            Log.d("RegisterActivity", "Try to register")
            AuthService.registerUser( email , password, userName){ registerSuccess ->
                    if(registerSuccess){
                        Log.d("RegisterActivity", "Register Success, try to login")
                        AuthService.loginUser(email, password) { loginSuccess ->
                            if (loginSuccess) {
                                Log.d("RegisterActivity", "Login Success")
                                val userDataChange = Intent(BROADCAST_USER_LOGGED_IN)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                enableSpinner(false)
                                finish()
                            } else {
                                Log.d("RegisterActivity", "Login Failed")
                                errorToast()
                            }
                        }
                    } else {
                        Log.d("RegisterActivity", "Register Failed")
                        errorToast()
                    }
                }
        }
        else   {
            Toast.makeText(this, "Fields are not valid", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }
    }


    private fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean){
        if(enable){
            createSpinner.visibility = View.VISIBLE
        }
        else{
            createSpinner.visibility = View.INVISIBLE
        }
        createUserBtn.isEnabled = !enable
    }

    private fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

}
