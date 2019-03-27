package com.rohail.androidudp.network

import android.content.Context
import com.rohail.androidudp.interfaces.MsgCallback
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class Client(
    val context: Context,
    val msgCallback: MsgCallback,
    strNetworkIP: String
) : Runnable {

    private var worker: Thread? = Thread(this)
    private val running = AtomicBoolean(false)
    private var interval: Int = 0
    private val port = 7776
    var strNetworkIP = strNetworkIP

    fun setIP(ip: String) {
        this.strNetworkIP = ip
    }

    private var msgString: String = "";

    fun sendMessage(msg: String) {
        this.msgString = msg
    }

    fun ControlSubThread(sleepInterval: Int) {
        interval = sleepInterval
    }

    fun start() {
        worker!!.start()
    }

    fun stop() {
        running.set(false)
        worker!!.interrupt()
    }

    override fun run() {
        running.set(true)
        try {
            val host = strNetworkIP

            val message = msgString.toByteArray()

            // Get the internet address of the specified host
            val address = InetAddress.getByName(host)

            // Initialize a datagram packet with data and address
            val packet = DatagramPacket(
                message, message.size,
                address, port
            )

            // Create a datagram socket, send the packet through it, close it.
            val dsocket = DatagramSocket()
            dsocket.send(packet)
            dsocket.close()
            println("Sent")
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
}