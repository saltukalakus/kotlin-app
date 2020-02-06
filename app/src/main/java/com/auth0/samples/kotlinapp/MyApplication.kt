package com.auth0.samples.kotlinapp

import android.app.Application
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage

class MyApplication : Application() {

    companion object {

        private lateinit var auth0: Auth0
        private lateinit var credentialsManager: SecureCredentialsManager

        fun getAuth0() = auth0
        fun getCredentialsManager() = credentialsManager
    }

    override fun onCreate() {
        super.onCreate()

        initAuth0()
        initCredentialsManager()
    }

    private fun initAuth0() {
        auth0 = Auth0(getString(R.string.auth0ClientId), getString(R.string.auth0Domain)).also {
            it.isOIDCConformant = true
            it.isLoggingEnabled = BuildConfig.DEBUG
        }
    }

    private fun initCredentialsManager() {
        credentialsManager = SecureCredentialsManager(this,
            AuthenticationAPIClient(auth0),
            SharedPreferencesStorage(this))
    }
}
