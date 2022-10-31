package com.esportsarena.liveapibroadcast

import java.io.DataOutputStream
import java.net.*

var socketOut: DataOutputStream? = null

val isSocketConnected: Boolean
    get() { return socketOut != null }

fun main(args: Array<String>) {
    val gui = GUI()

    awaitSocketAddress()

    ApexEventReader.start()
}

fun sendEventToServer(content: String) {
    try {
        socketOut?.writeUTF(content)
    } catch (e: SocketException) {
        println(">>> Connection to Server Lost.")
        socketOut = null
        Thread { awaitSocketAddress() }.start()
    }
}

fun awaitSocketAddress() {
    println(">>> Awaiting discovery packet...")
    val socket = DatagramSocket(8880, InetAddress.getByName("0.0.0.0"))
    socket.broadcast = true

    val recvBuf = ByteArray(15000)
    val packet = DatagramPacket(recvBuf, recvBuf.size)

    while (true) {
        socket.receive(packet)

        println(">>> Packet received; data: ${String(packet.data).trim()}")

        val message = String(packet.data).trim()
        if (message.startsWith("APEXREADER_SERVER_BROADCAST")) {
            val eventSocket = Socket(packet.address, 8884)
            socketOut = DataOutputStream(eventSocket.getOutputStream())

            println(">>> Address Found: ${packet.address}")
            socket.close()
            break
        }
    }
}