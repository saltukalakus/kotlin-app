package com.auth0.samples.kotlinapp

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.android.volley.toolbox.Volley
import com.auth0.android.Auth0
import com.auth0.android.provider.WebAuthProvider
import com.auth0.samples.kotlinapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CredentialsManager.setContext(this)

        // setting up a Volley RequestQueue
        val queue = Volley.newRequestQueue(this)

        // referencing the binding object of the view
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // loggedIn should be false by default to show the button
        binding?.loggedIn = false

        // getting a reference for the ListView
        val listToDo = findViewById(R.id.list_todo) as ListView

        // passing the activity, the queue and the ListView to the function
        // that consumes the RESTful endpoint
        getItems(this, queue, listToDo)

        // triggering the login method when the button is clicked
        val loginButton = findViewById(R.id.login_button)
        loginButton.setOnClickListener { login() }

        val addItemButton = findViewById(R.id.add_item)
        val itemEditText = findViewById(R.id.item) as EditText
        addItemButton.setOnClickListener {
            val item = itemEditText.text.toString()
            addItem(queue, item, CredentialsManager.getAccessToken(), {
                itemEditText.text.clear()
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
                getItems(this, queue, listToDo)
            })
        }
    }

    // Auth0 triggers an intent on a successful login
    override fun onNewIntent(intent: Intent) {
        if (WebAuthProvider.resume(intent)) {
            binding?.loggedIn = true
            return
        }
        super.onNewIntent(intent)
    }

    private fun login() {
        WebAuthProvider.init(Auth0("4jDhRaCvr2EBiGgR0JAtYX3SD8OkIr6g", "krebsapp.auth0.com"))
                .withScheme("demo")
                .withAudience("kotlin-todo-app")
                .start(this, AuthenticationHandler(this.applicationContext))
    }
}