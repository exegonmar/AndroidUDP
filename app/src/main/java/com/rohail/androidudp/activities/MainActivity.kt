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
//        btnServer.setOnClickListener({ startAdminActivity("Server") })
        btnClient.setOnClickListener({ startAdminActivity("Client") })
        btnScreen.setOnClickListener({ startIPActivity("Screen") })
    }

    private fun startAdminActivity(s: String) {
        val intent = Intent(this, AdminSequenceActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("KeyType", s)
        // start your next activity
        startActivity(intent)
    }

    private fun startIPActivity(s: String) {
        val intent = Intent(this, IPAddressActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("KeyType", s)
        // start your next activity
        startActivity(intent)
    }
}
