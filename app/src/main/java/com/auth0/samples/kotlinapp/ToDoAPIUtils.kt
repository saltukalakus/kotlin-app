package com.auth0.samples.kotlinapp

import android.app.Activity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONArray
import android.util.Log

val ENDPOINT = "http://10.0.2.2:8080/"

fun getItems(activity: Activity, queue: RequestQueue, listView: ListView) {
    val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, ENDPOINT, null,
            Response.Listener<JSONArray> { response ->
                val list = ArrayList<String>()

                (0 until response.length()).mapTo(list) {
                    response[it].toString()
                }

                val adapter = ArrayAdapter(activity,
                        android.R.layout.simple_list_item_1, list)
                listView.adapter = adapter
            },
            Response.ErrorListener { error ->
                Toast.makeText(

                        activity.applicationContext,
                        error.toString(),
                        Toast.LENGTH_SHORT).show()
            }
    )
    //add getItems to queue
    queue.add(jsonArrayRequest)
}