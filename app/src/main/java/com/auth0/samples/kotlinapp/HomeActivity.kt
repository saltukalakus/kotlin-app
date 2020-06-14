package com.auth0.samples.kotlinapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.Volley
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.jwt.JWT
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.activity_home.*
import com.auth0.samples.kotlinapp.MyApplication
import com.auth0.samples.kotlinapp.R
import com.auth0.samples.kotlinapp.MainActivity
import android.widget.Toast

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        // setting up a Volley RequestQueue
        val queue = Volley.newRequestQueue(this)
        setUpListeners()
        getItems(this, queue, list_todo);
    }

    private fun setUpListeners()
    {
        logoutBtn.setOnClickListener {
            // clear stored credentials
            MyApplication.getCredentialsManager().clearCredentials()

            // go to sign in screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_CLEAR_CREDENTIALS, true)

            // go to sign in screen
            startActivity(intent)
            this?.finish()
        }
        
        addItemBtn.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val item = itemEditText.text.toString()
            addItem(queue, item, CredentialsManager.getAccessToken(this), {
                itemEditText.text?.clear()
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
                getItems(this, queue, list_todo)
            })
        }

    }

    private fun getUserName() {
        MyApplication.getCredentialsManager().getCredentials(object : BaseCallback<Credentials?, CredentialsManagerException?> {
            override fun onSuccess(credentials: Credentials?) { //Use credentials
                var username: String? = null

                credentials?.idToken?.let {
                    username = JWT(it).getClaim("name").asString()
                }
            }

            override fun onFailure(error: CredentialsManagerException?) {
                error!!.printStackTrace()
            }
        })
    }
}
