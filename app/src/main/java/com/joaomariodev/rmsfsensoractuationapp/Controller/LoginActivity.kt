package com.joaomariodev.rmsfsensoractuationapp.Controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.joaomariodev.rmsfsensoractuationapp.R
import com.joaomariodev.rmsfsensoractuationapp.Services.AuthService
import com.joaomariodev.rmsfsensoractuationapp.Utilities.ConstantsO.BROADCAST_USER_LOGGED_IN
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSpinner.visibility = View.INVISIBLE
    }

    fun loginLoginBtnClicked(view: View){
        enableSpinner(true)
        val email = loginEmailTxt.text.toString()
        val password = loginPasswordTxt.text.toString()

        hideKeyboard()

        if(email.isNotEmpty() && password.isNotEmpty()){

            AuthService.loginUser(email, password){ loginSuccess ->
                if (loginSuccess) {
                    val userDataChange = Intent(BROADCAST_USER_LOGGED_IN)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                    enableSpinner(false)
                    finish()
                } else {
                    errorToast()
                }
            }

        }
        else {
            Toast.makeText(this, "The fields are invalid", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }


    }

    fun loginCreateUserBtnClicked(view: View){
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    private fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean){
        if(enable){
            loginSpinner.visibility = View.VISIBLE
        }
        else{
            loginSpinner.visibility = View.INVISIBLE
        }
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }

    private fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

}
