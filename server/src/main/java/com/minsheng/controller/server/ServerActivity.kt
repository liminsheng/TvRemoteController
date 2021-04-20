package com.minsheng.controller.server

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketAddress
import java.net.SocketException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class ServerActivity : AppCompatActivity() {

    private val mExecutor = Executors.newCachedThreadPool()
    private val mClientMap = HashMap<SocketAddress, Runnable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        startUdpServer()
    }

    private fun startUdpServer() {
        mExecutor.execute {
            try {
                val server = DatagramSocket(5557)
                val len = 1024
                val dataIn = ByteArray(len)
                val datagramPacketIn = DatagramPacket(dataIn, len)
                println("server 127.0.0.1准备接收数据...")
                for (i in 0..99) {
                    //接收client数据
                    server.receive(datagramPacketIn)
                    val socketAddress = datagramPacketIn.socketAddress
                    if (mClientMap[socketAddress] == null) {
                        //收到客户端连接后开启发送线程
                        val sendMsgRun = SendMsgRun(server, socketAddress)
                        mExecutor.execute(sendMsgRun)
                        mClientMap[socketAddress] = sendMsgRun
                    }
                    println("msg form client: " + String(dataIn, 0, datagramPacketIn.length))
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

internal class SendMsgRun(
    private val server: DatagramSocket,
    private val address: SocketAddress
) :
    Runnable {
    override fun run() {
        println("server 发送线程已开启...")
        try {
            var dataOut: ByteArray
            var datagramPacketOut: DatagramPacket? = null
            val sc = Scanner(System.`in`)
            while (sc.hasNextLine()) {
                dataOut = sc.nextLine().toByteArray()
                datagramPacketOut = DatagramPacket(dataOut, dataOut.size, address)
                //发送数据给client
                server.send(datagramPacketOut)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            println("server 异常了 " + e.message)
        }
    }

}