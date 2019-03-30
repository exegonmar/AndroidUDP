package com.rohail.androidudp.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private var client: Client? = null
    private var server: Server? = null
    private var thread: Thread? = null
    private var strNetworkIP = "255.255.255.255"

    private var typeExtra: String? = ""

    private var sequenceExtra: String = ""

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.WRITE_SETTINGS,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var ipExtra: String = intent.getStringExtra("keyIdentifier")
        typeExtra = intent.getStringExtra("KeyType")

        val sharedPref = this.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE) ?: return
        sequenceExtra = sharedPref.getString("sequence", "")

        this.timeString = sharedPref.getString("time", "")

        if (!TextUtils.isEmpty(ipExtra)) {
            strNetworkIP = ipExtra
        }

        if (typeExtra.equals("Screen")) {
            llRemote.visibility = View.GONE
            llMsg.visibility = View.VISIBLE
        } else {
            if (TextUtils.isEmpty(sequenceExtra) || TextUtils.isEmpty(this.timeString)) {
                Toast.makeText(this, "No proper sequence found", Toast.LENGTH_LONG).show()
            }

            this.parseSequence(this.sequenceExtra)
        }

        btnStart.setOnClickListener(this)
        tvDetail.movementMethod = ScrollingMovementMethod()

        startProcess()
    }

    private fun startProcess() {
        if (typeExtra.equals("Screen")) {
            thread = Thread(Runnable {
                try {
                    server = Server(this, this, this)
                    server!!.setIP(strNetworkIP)
                    server!!.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

            thread!!.start()
        }

        try {
            if (!typeExtra.equals("Screen")) {
                thread = Thread(Runnable {
                    client = Client(this, strNetworkIP)
                })

                thread!!.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun parseSequence(sequence: String) {
        sequenceString = sequence.split("\n").toTypedArray()
        var i: Int = 0
        for (seq in sequenceString) {
            if (seq.toLowerCase().contains("start")) {
//                sequenceString.set(i, seq + "\n" + "(a " + this.timeString + " ms pause)")
            } else if (seq.toLowerCase().contains("time:")) {
                timeString = seq.split("time:")[1]
                sequenceString = sequenceString.drop(i).toList().toTypedArray()
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
        /*if (client != null)
            client!!.stop()*/

        /*if (thread != null) {
            thread!!.interrupt()
            thread = null
        }*/

        var threadT = Thread(Runnable {
            try {
                client!!.sendMessage("time:" + timeString + "\n" + seq)
                client!!.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        threadT!!.start()
    }

    override fun onMsgReceived(msg: String) {
        var i: Int = 0
        var milliseconds: Long

        runOnUiThread {
            // Stuff that updates the UI
            parseSequence(msg)
            milliseconds = timeString.toLong()
            System.out.println("\n" + sequenceString[i])
            tvDetail.post {

                var handler = Handler()

                val runnable = object : Runnable {
                    override fun run() {

                        if (i < sequenceString.size) {

                            if (typeExtra.equals("Client")) {
                                tvDetail.text = sequenceString[i]
                            } else {
                                tvMsg.text = sequenceString[i]
                                if (sequenceString[i].toLowerCase().contains("fade")) {
                                    llMsg.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@UDPActivity,
                                            R.color.black_overlay
                                        )
                                    )
                                }else if (sequenceString[i].toLowerCase().contains("start")) {
                                    llMsg.setBackgroundColor(ContextCompat.getColor(this@UDPActivity, R.color.blue))
                                } else if (sequenceString[i].toLowerCase().contains("stop")) {
                                    llMsg.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@UDPActivity,
                                            R.color.colorAccent
                                        )
                                    )
                                } else {
                                    llMsg.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@UDPActivity,
                                            R.color.colorPrimary
                                        )
                                    )
                                }
                            }
                        }

                        i++
                        if (i < sequenceString.size) {
                            handler.postDelayed(this, milliseconds)
                        }
                    }
                }

                handler.post(runnable)
            }
        }
    }

    override fun generateIPCallback(ip: String) {
        strNetworkIP = ip
        try {
            if (!typeExtra.equals("Screen")) {
                client = Client(this, strNetworkIP)
                client!!.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (this.server != null)
            this.server!!.stop()
        if (this.client != null)
            this.client!!.stop()
        super.onBackPressed()
    }
}
