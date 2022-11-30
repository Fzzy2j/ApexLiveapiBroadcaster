package com.esportsarena.liveapibroadcast

import java.io.DataOutputStream
import java.net.*

var socketOut: DataOutputStream? = null
lateinit var gui: GUI

val isSocketConnected: Boolean
    get() { return socketOut != null }

fun main(args: Array<String>) {
    gui = GUI()

    awaitSocketAddress()

    ApexEventReader.start()
}

var lastEventSend = System.currentTimeMillis()
fun sendEventToServer(content: String) {
    try {
        socketOut?.writeUTF(content)
        lastEventSend = System.currentTimeMillis()
    } catch (e: SocketException) {
        println(">>> Connection to Server Lost.")
        socketOut = null
        Thread { awaitSocketAddress() }.start()
    }
}

fun awaitSocketAddress() {
    println(">>> Awaiting discovery packet...")
    gui.isConnected = false
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
            Thread {
                while (true) {
                    Thread.sleep(5000)
                    if (System.currentTimeMillis() - lastEventSend > 5000)
                    sendEventToServer("LIVEAPIBROADCAST_HEARTBEAT")
                    if (socketOut == null) break
                }
            }.start()
            socketOut = DataOutputStream(eventSocket.getOutputStream())


            println(">>> Address Found: ${packet.address}")
            socket.close()
            gui.isConnected = true
            break
        }
    }
}