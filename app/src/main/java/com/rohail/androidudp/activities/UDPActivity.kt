package com.rohail.androidudp.activities

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rohail.androidudp.interfaces.MsgCallback
import com.rohail.androidudp.interfaces.NetworkIPInterface
import com.rohail.androidudp.network.Client
import com.rohail.androidudp.network.Server
import kotlinx.android.synthetic.main.activity_main.*


class UDPActivity : AppCompatActivity(), View.OnClickListener, MsgCallback, NetworkIPInterface {

    private var sequenceString1 = arrayOf(
        "start video1\n",
        "    (a {0} ms pause)\n",
        "    stop video1\n" +
                "    start video2\n",
        "    (a {0} ms pause)\n",
        "    start fade\n",
        "    (a {0} ms pause)\n",
        "    stop fade\n" +
                "    stop video2"
        /*"start video2\n" +
                "    (a {0} ms pause)\n" +
                "    stop video2\n" +
                "    start video1\n" +
                "    (a {0} ms pause)\n" +
                "    start fade\n" +
                "    (a {0} ms pause)\n" +
                "    stop fade\n" +
                "    stop video1"*/
    )
    private lateinit var client: Client
    private lateinit var server: Server
    private var thread: Thread? = null
    private var strNetworkIP = "0.0.0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                sendMessage(0, etVideo1.text.toString())
            }
            btnVideo2.id -> {
                sendMessage(1, etVideo2.text.toString())
            }
        }
    }

    private fun sendMessage(seqNumber: Int, milliseconds: String) {
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
        return seqNumber.toString()
    }

    override fun onMsgReceived(msg: String) {
        val handler = Handler()
        val milliseconds: Long = etVideo1.text.toString().toLong()

        runOnUiThread {
            // Stuff that updates the UI
            for (message in sequenceString1) {
                if (message.contains("{0}")) {
                    message = message.replace("{0}", milliseconds.toString())
                }
                handler.postDelayed(Runnable {
                    tvDetail.setText(tvDetail.text.toString() + "\n\n\n" + message)
                }, milliseconds)
            }
        }
    }

    override fun generateIPCallback(ip: String) {
        strNetworkIP = ip
        client = Client(this, this, strNetworkIP)
    }

}
