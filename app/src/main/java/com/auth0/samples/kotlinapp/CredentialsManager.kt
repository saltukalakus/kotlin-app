package com.auth0.samples.kotlinapp

import android.content.Context
import com.auth0.android.result.Credentials

object CredentialsManager {
    private var context: Context? = null
    private val PREFERENCES_NAME = "auth0"
    private val ACCESS_TOKEN = "access_token"

    fun setContext(context: Context) {
        this.context = context
    }

    fun saveCredentials(credentials: Credentials) {
        val sp = context?.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE)

        sp!!.edit().putString(ACCESS_TOKEN, credentials.accessToken)
                .apply()
    }

    fun getAccessToken(): String {
        val sp = context?.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE)

        return sp!!.getString(ACCESS_TOKEN, null)
    }
}