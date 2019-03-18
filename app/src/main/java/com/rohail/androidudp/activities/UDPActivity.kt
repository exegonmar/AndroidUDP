package com.rohail.androidudp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.text.method.ScrollingMovementMethod
import com.rohail.androidudp.interfaces.MsgCallback
import com.rohail.androidudp.interfaces.NetworkIPInterface
import com.rohail.androidudp.network.Client
import com.rohail.androidudp.network.Server


class UDPActivity : AppCompatActivity(), View.OnClickListener, MsgCallback, NetworkIPInterface {

    private val sequenceString = arrayOf(
        "start video1\n" +
                "    (a 500 ms pause)\n" +
                "    stop video1\n" +
                "    start video2\n" +
                "    (a 1200 ms pause)\n" +
                "    start fade\n" +
                "    (a 200 ms pause)\n" +
                "    stop fade\n" +
                "    stop video2",
        "start video2\n" +
                "    (a 500 ms pause)\n" +
                "    stop video2\n" +
                "    start video1\n" +
                "    (a 1200 ms pause)\n" +
                "    start fade\n" +
                "    (a 200 ms pause)\n" +
                "    stop fade\n" +
                "    stop video1"
    )
    private lateinit var client: Client
    private lateinit var server: Server
    private var thread: Thread? = null
    private var strNetworkIP = "0.0.0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.rohail.androidudp.R.layout.activity_main)

        var ipExtra: String = intent.getStringExtra("keyIdentifier")

        if (!TextUtils.isEmpty(ipExtra)) {
            strNetworkIP = ipExtra
        }

        btnVideo1.setOnClickListener(this)
        btnVideo2.setOnClickListener(this)
        tvDetail.movementMethod = ScrollingMovementMethod()

        val thread = Thread(Runnable {
            try {
                server = Server(this, this, this)
                server.setIP(strNetworkIP)
                server.start()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread.start()
    }

    override fun onClick(v: View?) {

        when (v!!.id) {
            btnVideo1.id -> {
                sendMessage(0)
            }
            btnVideo2.id -> {
                sendMessage(1)
            }
        }
    }

    private fun sendMessage(seqNumber: Int) {
        client.stop()

        if (thread != null)
            thread!!.interrupt()

        thread = Thread(Runnable {
            try {
                client.sendMessage(getSequence(seqNumber))
                client.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread!!.start()
    }

    private fun getSequence(seqNumber: Int): String {
        return sequenceString[seqNumber]
    }

    override fun onMsgReceived(msg: String) {
        runOnUiThread {
            // Stuff that updates the UI
            tvDetail.setText(tvDetail.text.toString() + "\n\n\n" + msg)
        }
    }

    override fun generateIPCallback(ip: String) {
        strNetworkIP = ip
        client = Client(this, this, strNetworkIP)
    }

}
