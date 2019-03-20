package com.rohail.androidudp.activities

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rohail.androidudp.R
import com.rohail.androidudp.interfaces.MsgCallback
import com.rohail.androidudp.interfaces.NetworkIPInterface
import com.rohail.androidudp.network.Client
import com.rohail.androidudp.network.Server
import kotlinx.android.synthetic.main.activity_main.*

class UDPActivity : AppCompatActivity(), View.OnClickListener, MsgCallback, NetworkIPInterface {

    private var sequenceString1 = arrayOf(
        "start video1\n",
        "(a {0} ms pause)\n",
        "stop video1\n" +
                "start video2\n",
        "(a {0} ms pause)\n",
        "start fade\n",
        "(a {0} ms pause)\n",
        "stop fade\n" +
                "stop video2"
    )

    private var sequenceString2 = arrayOf(
        "start video2\n",
        "a {0} ms pause)\n",
        "stop video2\n" +
                "start video1\n",
        "(a {0} ms pause)\n",
        "start fade\n",
        "(a {0} ms pause)\n",
        "stop fade\n" +
                "stop video1"
    )
    private lateinit var client: Client
    private lateinit var server: Server
    private var thread: Thread? = null
    private var strNetworkIP = "0.0.0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var ipExtra: String = intent.getStringExtra("keyIdentifier")
        var typeExtra: String = intent.getStringExtra("KeyType")

        if (typeExtra.equals("Server")) {
            btnVideo1.visibility = View.INVISIBLE
            btnVideo2.visibility = View.INVISIBLE
            etVideo1.visibility = View.INVISIBLE
            etVideo2.visibility = View.INVISIBLE
            tvms1.visibility = View.INVISIBLE
            tvms2.visibility = View.INVISIBLE
        }

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
        var i: Int = 0
        var milliseconds: Long
        var finalMessage: String = ""
        var sequenceText = arrayOf("")
        if (msg.equals("0")) {
            sequenceText = sequenceString1
            milliseconds = etVideo1.text.toString().toLong()
        } else {
            sequenceText = sequenceString2
            milliseconds = etVideo2.text.toString().toLong()
        }

        runOnUiThread {
            // Stuff that updates the UI
            val time = System.currentTimeMillis()

            System.out.println("\n" + sequenceText[i])
            val wait = time + milliseconds - System.currentTimeMillis()
            tvDetail.post(Runnable {

                var handler = Handler()

                val runnable = object : Runnable {
                    override fun run() {

                        if (i < sequenceText.size) {
                            if (sequenceText[i].contains("{0}")) {
                                finalMessage =
                                    tvDetail.text.toString() + sequenceText[i].replace("{0}", milliseconds.toString())
                            } else {
                                finalMessage = tvDetail.text.toString() + sequenceText[i]
                            }
                            tvDetail.text = finalMessage
                        }

                        i++
                        if (i < sequenceText.size)
                            handler.postDelayed(this, milliseconds)
                    }
                }

                handler.post(runnable)


            })
        }
    }


    override fun generateIPCallback(ip: String) {
        strNetworkIP = ip
        client = Client(this, this, strNetworkIP)
    }

}
