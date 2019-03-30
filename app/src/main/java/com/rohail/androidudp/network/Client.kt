package com.rohail.androidudp.network

import android.content.Context
import com.rohail.androidudp.interfaces.MsgCallback
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import android.net.wifi.WifiManager


class Client(
    val context: Context,
    strNetworkIP: String
) {

    private val port = 58452
    private var strNetworkIP = strNetworkIP
    private var dsocket: DatagramSocket? = null

    fun setIP(ip: String) {
        this.strNetworkIP = ip
    }

    private var msgString: String = "";

    fun sendMessage(msg: String) {
        this.msgString = msg
    }

    fun stop() {
        dsocket!!.close()
    }

    fun start() {
        try {
            val message = msgString.toByteArray()

            // Get the internet address of the specified host
            val address = InetAddress.getByName(strNetworkIP)

            // Initialize a datagram packet with data and address
            val packet = DatagramPacket(
                message, message.size,
                address, port
            )

            if (dsocket == null || dsocket!!.isClosed) {
                dsocket = DatagramSocket()
                dsocket!!.broadcast = true
            }
            dsocket!!.send(packet)

            println("Sent")
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
}