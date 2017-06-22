package com.auth0.samples.kotlinapp

import android.app.Dialog
import android.content.Context
import android.widget.Toast
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.result.Credentials

class AuthenticationHandler(val context: Context) : AuthCallback {
    override fun onFailure(dialog: Dialog) {
        val text = "Ops, something went wrong!"
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(exception: AuthenticationException) {
        val text = "Ops, something went wrong!"
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onSuccess(credentials: Credentials) {
        CredentialsManager.saveCredentials(credentials)
    }
}