package com.rohail.androidudp.network

import android.content.Context
import android.net.wifi.WifiManager
import android.text.TextUtils
import com.rohail.androidudp.interfaces.MsgCallback
import com.rohail.androidudp.interfaces.NetworkIPInterface
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class Server(val context: Context, val msgCallback: MsgCallback, val ipCallback: NetworkIPInterface) : Runnable {

    private var worker: Thread? = null
    private val running = AtomicBoolean(false)
    private var interval: Int = 0
    private val port = 58452
    var strNetworkIP = ""

    fun setIP(ip: String) {
        this.strNetworkIP = ip
    }

    @Throws(IOException::class)
    fun getBroadcastAddress(): InetAddress {
        val wifi = this.context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifi.dhcpInfo
        // handle null somehow

        val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3)
            quads[k] = (broadcast shr k * 8 and 0xFF).toByte()
        return InetAddress.getByAddress(quads)
    }

    fun ControlSubThread(sleepInterval: Int) {
        interval = sleepInterval
    }

    fun start() {
        worker = Thread(this)
        worker!!.start()
    }

    fun stop() {
        running.set(false)
        worker!!.interrupt()
    }

    override fun run() {
        running.set(true)
        if (TextUtils.isEmpty(strNetworkIP)) {
            strNetworkIP = getBroadcastAddress().hostAddress
        }
        ipCallback.generateIPCallback(strNetworkIP)
        try {
            // Create a socket to listen on the port.
            val dsocket = DatagramSocket(port)

            // Create a buffer to read datagrams into. If a
            // packet is larger than this buffer, the
            // excess will simply be discarded!
            val buffer = ByteArray(2048)

            // Create a packet to receive data into the buffer
            val packet = DatagramPacket(buffer, buffer.size)

            // Now loop forever, waiting to receive packets and printing them.
            while (true) {
                // Wait to receive a datagram
                dsocket.receive(packet)

                // Convert the contents to a string, and display them
                val msg = String(buffer, 0, packet.length)
                println(
                    packet.address.hostName + ": "
                            + msg
                )
                msgCallback.onMsgReceived(
                    msg
                )

                // Reset the length of the packet before reusing it.
                packet.length = buffer.size
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
}
