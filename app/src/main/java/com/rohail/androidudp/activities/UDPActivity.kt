package com.rohail.androidudp.activities

import android.content.Context
import android.net.wifi.WifiManager
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
import java.io.IOException
import java.net.InetAddress


class UDPActivity : AppCompatActivity(), View.OnClickListener, MsgCallback, NetworkIPInterface {

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

//        client = Client(this, this)
//        var udpServer: UDPServer = UDPServer()
//        udpServer.start(this)
    }

    @Throws(IOException::class)
    fun getBroadcastAddress(): InetAddress {
        val wifi = this.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifi.dhcpInfo
        // handle null somehow

        val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3)
            quads[k] = (broadcast shr k * 8 and 0xFF).toByte()
        return InetAddress.getByAddress(quads)
    }

    override fun onClick(v: View?) {

        when (v!!.id) {
            btnVideo1.id -> {
                sendMessage()
            }
            btnVideo2.id -> {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        client.stop()

        if (thread != null)
            thread!!.interrupt()

        thread = Thread(Runnable {
            try {
                client.sendMessage(getSequence1())
                client.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread!!.start()
    }

    private fun getSequence1(): String {
        return "start video1\n" +
                "    (a 500 ms pause)\n" +
                "    stop video1\n" +
                "    start video2\n" +
                "    (a 1200 ms pause)\n" +
                "    start fade\n" +
                "    (a 200 ms pause)\n" +
                "    stop fade\n" +
                "    stop video2"
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
