package com.rohail.androidudp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.rohail.androidudp.R
import kotlinx.android.synthetic.main.activity_ipaddress.*
import kotlinx.android.synthetic.main.activity_start.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        btnServer.setOnClickListener(View.OnClickListener { startIPActivity("Server") })
        btnClient.setOnClickListener(View.OnClickListener { startIPActivity("Client") })
        btnScreen.setOnClickListener(View.OnClickListener { startIPActivity("Screen") })
    }

    private fun startIPActivity(s: String) {
        val intent = Intent(this, IPAddressActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("KeyType", s)
        // start your next activity
        startActivity(intent)
    }
}
