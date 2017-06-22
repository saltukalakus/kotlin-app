package com.auth0.samples.kotlinapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setting up a Volley RequestQueue
        val queue = Volley.newRequestQueue(this)

        // getting a reference for the ListView
        val listToDo = findViewById(R.id.list_todo) as ListView

        // passing the activity, the queue and the ListView to the function
        // that consumes the RESTful endpoint
        getItems(this, queue, listToDo)
    }
}