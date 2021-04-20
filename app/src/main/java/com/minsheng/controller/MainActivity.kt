package com.minsheng.controller

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.minsheng.controller.databinding.ActivityMainBinding
import com.minsheng.controller.view.DirectionDpadView
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val mExecutor = Executors.newCachedThreadPool()
    private var mClient: DatagramSocket? = null

    private lateinit var mDataBinding: ActivityMainBinding
    private val mLiveData = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
        startUdpClient()
    }

    private fun initView() {
        mLiveData.observe(this, Observer {
            mDataBinding.tips = it
        })
        mDataBinding.apply {
            setOnShotDownClick {
                mLiveData.value = "您点击了关机键"
            }
            setOnVolumeDownClick {
                mLiveData.value = "您点击了音量减键"
            }
            setOnVolumeUpClick {
                mLiveData.value = "您点击了音量加键"
            }
            setOnHomeClick {
                mLiveData.value = "您点击了主页键"
            }
            setOnMenuClick {
                mLiveData.value = "您点击了菜单键"
            }
            setOnBackClick {
                mLiveData.value = "您点击了返回键"
            }
            directionDpadListener = object : DirectionDpadView.OnDirectionKeyListener {
                override fun onClick(keyCode: Int) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            mLiveData.value = "您点击了确定键"
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            mLiveData.value = "您点击了上键"
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            mLiveData.value = "您点击了下键"
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            mLiveData.value = "您点击了左键"
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            mLiveData.value = "您点击了右键"
                        }
                    }
                }

                override fun onLongPress(keyCode: Int) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            mLiveData.value = "您长按了确定键"
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            mLiveData.value = "您长按了上键"
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            mLiveData.value = "您长按了下键"
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            mLiveData.value = "您长按了左键"
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            mLiveData.value = "您长按了右键"
                        }
                    }

                }
            }
        }
    }

    private fun startUdpClient() {
        mExecutor.execute {
            try {
                val address = InetSocketAddress("192.168.2.184", 5557)
                var dataOut: ByteArray? = null
                //发送数据需要address
                val datagramPacketOut =
                    DatagramPacket(ByteArray(0), 0, address)
                mClient = DatagramSocket().run {
                    println("client端：")
                    mExecutor.execute(ReceiveMsgRun(this))
//                val scanner = Scanner(System.`in`)
//                while (scanner.hasNextLine()) {
//                    dataOut = scanner.nextLine().toByteArray()
                    dataOut = "你好！".toByteArray()
                    //获取键盘输入数据
                    datagramPacketOut.data = dataOut
                    //发送数据给server
                    this.send(datagramPacketOut)
                    this
//                }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mClient?.isClosed != true) {
            mClient?.close()
        }
        mExecutor.shutdown()
    }
}

internal class ReceiveMsgRun(private val client: DatagramSocket) : Runnable {
    override fun run() {
        val len = 1024
        val dataIn = ByteArray(len)
        //接收数据
        val datagramPacketIn = DatagramPacket(dataIn, len)
        for (i in 0..99) {
            try {
                //接收server发来的数据
                client.receive(datagramPacketIn)
                println("msg form server: " + String(dataIn, 0, datagramPacketIn.length))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}