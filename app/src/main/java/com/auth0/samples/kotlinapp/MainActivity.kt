package com.auth0.samples.kotlinapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.volley.toolbox.Volley
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
// this only works if you have a data field in the main activity xml
// it can be created with Alt + Enter
import com.auth0.samples.kotlinapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // referencing the binding object of the view
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // loggedIn should be false by default to show the button
        binding?.loggedIn = false

        // triggering the login method when the button is clicked
        val loginButton = binding?.loginButton
        loginButton?.setOnClickListener { login() }
    }

    // Auth0 triggers an intent on a successful login
    override fun onNewIntent(intent: Intent) {
        if (WebAuthProvider.resume(intent)) {
            return
        }
        super.onNewIntent(intent)
    }

    // source for the login with auth0:
    // https://auth0.com/blog/authenticating-android-apps-developed-with-kotlin/
    private fun login() {
        val account = Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain))
        account.isOIDCConformant = true

        WebAuthProvider.init(account)
                .withScheme("login")
                // the auth audience API may need to include https://
                .withAudience("auth0audienceAPI")
                .start(this, object : AuthCallback {
                    override fun onFailure(dialog: Dialog) {
                        runOnUiThread { dialog.show() }
                    }

                    override fun onFailure(exception: AuthenticationException) {
                        runOnUiThread {
                            Toast.makeText(
                                    this@MainActivity, "Something went wrong!",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onSuccess(credentials: Credentials) {
                        CredentialsManager.saveCredentials(this@MainActivity, credentials)
                        binding?.loggedIn = true
                        this@MainActivity.afterLoginSuccess()
                    }
                })
    }

    // this first example can run the nodejs server from the auth0 kotlin demo
    private fun afterLoginSuccess() {
        val listToDo = binding?.listTodo

        // setting up a Volley RequestQueue
        val queue = Volley.newRequestQueue(this@MainActivity)

        // getting a reference for the ListView
        // passing the activity, the queue and the ListView to the function
        // that consumes the REST-ful endpoint
        getItems(this@MainActivity, queue, listToDo!!)
    }
}
