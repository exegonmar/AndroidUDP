package com.rohail.androidudp.activities

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rohail.androidudp.BuildConfig
import com.rohail.androidudp.R
import com.rohail.androidudp.interfaces.MsgCallback
import com.rohail.androidudp.interfaces.NetworkIPInterface
import com.rohail.androidudp.network.Client
import com.rohail.androidudp.network.Server
import kotlinx.android.synthetic.main.activity_main.*

class UDPActivity : AppCompatActivity(), View.OnClickListener, MsgCallback, NetworkIPInterface {

    private var sequenceString = arrayOf("")
    private var timeString: String = ""

    private lateinit var client: Client
    private lateinit var server: Server
    private var thread: Thread? = null
    private var strNetworkIP = "0.0.0.0"

    private var typeExtra: String? = ""

    private var sequenceExtra: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var ipExtra: String = intent.getStringExtra("keyIdentifier")
        typeExtra = intent.getStringExtra("KeyType")

        val sharedPref = this.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE) ?: return
        sequenceExtra = sharedPref.getString("sequence", "")

        this.timeString = sharedPref.getString("time", "")

        if (TextUtils.isEmpty(sequenceExtra) || TextUtils.isEmpty(this.timeString)) {
            Toast.makeText(this, "No proper sequence found", Toast.LENGTH_LONG).show()
        }

        this.parseSequence(this.sequenceExtra)

        if (!TextUtils.isEmpty(ipExtra)) {
            strNetworkIP = ipExtra
        }

        if (typeExtra.equals("Client")) {
            llRemote.visibility = View.GONE
            llMsg.visibility = View.VISIBLE
        }

        btnStart.setOnClickListener(this)
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

    private fun parseSequence(sequenceExtra: String) {
        sequenceString = sequenceExtra.split("\n").toTypedArray()
        var i: Int = 0
        for (seq in sequenceString) {
            if (seq.toLowerCase().contains("start")) {
                sequenceString.set(i, seq + "\n" + "(a " + this.timeString + " ms pause)")
            }
            i++
        }
    }

    override fun onClick(v: View?) {

        when (v!!.id) {
            btnStart.id -> {
                sendMessage(sequenceExtra)
            }
        }
    }

    private fun sendMessage(seq: String) {
        if (client != null)
            client.stop()

        if (thread != null)
            thread!!.interrupt()

        thread = Thread(Runnable {
            try {
                client.sendMessage(seq)
                client.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread!!.start()
    }

    override fun onMsgReceived(msg: String) {
        var i: Int = 0
        var milliseconds: Long
        milliseconds = timeString.toLong()

        runOnUiThread {
            // Stuff that updates the UI
            val time = System.currentTimeMillis()

            System.out.println("\n" + sequenceString[i])
            tvDetail.post {

                var handler = Handler()

                val runnable = object : Runnable {
                    override fun run() {

                        if (i < sequenceString.size) {

                            if (typeExtra.equals("Client")) {
                                tvMsg.text = sequenceString[i]
                            }else {
                                tvDetail.text = sequenceString[i]
                            }
                        }

                        i++
                        if (i < sequenceString.size)
                            handler.postDelayed(this, milliseconds)
                    }
                }

                handler.post(runnable)


            }
        }
    }

    override fun generateIPCallback(ip: String) {
        strNetworkIP = ip
        client = Client(this, this, strNetworkIP)
        /*if (typeExtra.equals("Client")) {
            this.sendMessage(sequenceExtra)
        }*/
    }

    override fun onBackPressed() {
        this.server.stop()
        this.client.stop()
        super.onBackPressed()
    }

}
