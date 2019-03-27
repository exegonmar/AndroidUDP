package com.rohail.androidudp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.rohail.androidudp.BuildConfig
import com.rohail.androidudp.R
import kotlinx.android.synthetic.main.activity_admin_sequence.*

class AdminSequenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_sequence)
        etName.setText(
            "start video1\n" +
                    "stop video1\n" +
                    "start video2\n" +
                    "start fade\n" +
                    "stop fade\n" +
                    "stop video2"
        )
        etMS.setText("1000")
        btnSave.setOnClickListener({ startAdminActivity(intent.getStringExtra("KeyType")) })
    }

    private fun startAdminActivity(s: String) {

        val sharedPref = this.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("sequence", etName.text.toString())
            commit()
        }
        with(sharedPref.edit()) {
            putString("time", etMS.text.toString())
            commit()
        }

        val intent = Intent(this, IPAddressActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("KeyType", s)
        intent.putExtra("sequence", etName.text.toString())
        intent.putExtra("time", etMS.text.toString())
        // start your next activity
        startActivity(intent)
    }
}
