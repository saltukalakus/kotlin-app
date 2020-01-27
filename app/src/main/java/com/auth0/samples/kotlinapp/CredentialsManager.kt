package com.auth0.samples.kotlinapp

import android.content.Context
import com.auth0.android.result.Credentials

/*
source:
https://auth0.com/blog/authenticating-android-apps-developed-with-kotlin/
 */
object CredentialsManager {
    private const val PREFERENCES_NAME = "auth0"
    private const val ACCESS_TOKEN = "access_token"

    fun saveCredentials(context: Context, credentials: Credentials) {
        val sp = context.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE)

        sp!!.edit().putString(ACCESS_TOKEN, credentials.accessToken)
                .apply()
    }

    fun getAccessToken(context: Context): String? {
        val sp = context.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE)

        return sp!!.getString(ACCESS_TOKEN, null)
    }
}
