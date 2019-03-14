package com.rohail.androidudp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rohail.androidudp.R
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MsgCallback {

    private var server: Server? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        server = Server(this, this)
        tvInfoip!!.setText(server!!.getIpAddress() + ":" + server!!.getPort())
    }

    override fun onMsgReceived(msg: String) {
        tvMsg.setText(msg);
    }
}
