package com.minsheng.controller.util

import android.os.Build
import android.util.Log
import android.view.KeyEvent
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util.concurrent.Executors

/**
 * ClassName:      NetUtil
 * Author:         LiMinsheng
 * Date:           2021/4/20 15:09
 * Description:    网络工具
 */
object NetUtil {

    private const val TAG = "NetUtil"
    private const val MAX_THREAD_NUM = 10
    const val STTP_LOAD_TYPE_IR_KEY = 51
    const val STTP_LOAD_TYPE_BROADCAST = 52
    const val STTP_LOAD_TYPE_CMD_INPUT_TEXT = 53
    const val STTP_LOAD_TYPE_CMD_REVEIVE = 54
    const val STTP_LOAD_TYPE_CMD_VIRTUAL_MOUSE = 55
    const val STTP_LOAD_TYPE_CMD_FILE = 56
    const val STTP_LOAD_TYPE_CMD_VIDEO = 57
    const val STTP_LOAD_TYPE_CMD_MUSIC = 58

    /*54~72预留添加更多的命令*/
    const val STTP_LOAD_TYPE_CMD_XXX = 73
    const val STTP_LOAD_TYPE_REQUEST_CONNECTION = 120
    const val STTP_LOAD_TYPE_REQUEST_DISCONNECTION = 121
    const val STTP_LOAD_TYPE_REQUEST_CONNECTSTATUS = 122
    const val NORMAL_KEY = 0
    const val LONG_KEY_START = 1
    const val LONG_KEY_END = 2
    const val NEED_REPLY = 1
    private const val DATA_PACKET_TITLE_SIZE = 10
    private const val DATA_PACKET_SIZE = 1400
    private const val DATA_PACKET_BODY_INDEX = 10

    private val mPool by lazy {
        Executors.newCachedThreadPool()
    }
    private var mReceiverSocket: DatagramSocket? = null
    private var mSendSocket: DatagramSocket? = null
    private var mReceiverRunnable: ReceiverRunnable? = null
    private var mLongKeyRunnable: LongKeyRunnable? = null
    private val mIpDevMap = HashMap<String, String>()
    private var mRandomCounter = 0 /*循环计数器,用于标识每个数据包,0~254*/

    fun init() {
        Log.i(TAG, "init.")
        mReceiverRunnable = ReceiverRunnable()
        mPool.submit(mReceiverRunnable)
    }

    fun isRunning(): Boolean {
        return mReceiverSocket?.isConnected == true && mReceiverRunnable?.isFlag == true
    }

    fun release() {
        Log.i(TAG, "close all socket and release all resources.")
        mIpDevMap.clear()
        if (mReceiverSocket != null && !mReceiverSocket!!.isClosed) {
            mReceiverRunnable?.isFlag = false
            mReceiverRunnable = null
            mReceiverSocket?.close()
            mReceiverSocket = null
        }
        if (mSendSocket != null && !mSendSocket!!.isClosed) {
            mSendSocket?.close()
            mSendSocket = null
        }
    }

    /**
     * 解析接收的数据
     */
    private fun parseReceiveBuffer(buffer: ByteArray, datagramPacket: DatagramPacket) {
        val version: Int = buffer[0].toInt() and 0xC0 shr 6
        val deviceId: Int = buffer[0].toInt() and 0x3f
        val load_type: Int = buffer[1].toInt() and 0x7F
        val receive_flag: Int = buffer[1].toInt() and 0x80 shr 7
        val sn: Int = buffer[2].toInt() and 0xFF or (buffer[3].toInt() and 0xFF shl 8)
        val UUID: Int = buffer[4].toInt() and 0xFF
        Log.i(
            TAG, "parseReceiveBuffer::version[" + version + "] deviceId[" + deviceId
                    + "] load_type[" + load_type + "] SN[" + sn + "] receive_flag[" + receive_flag + "] UUID[" + UUID + "]"
        )

        if (receive_flag == NEED_REPLY) {
            send(
                buffer,
                datagramPacket.length - DATA_PACKET_TITLE_SIZE,
                datagramPacket.address.hostAddress
            )
        }

        if (load_type == STTP_LOAD_TYPE_IR_KEY && deviceId == 55) {
            val keyCode: Int =
                buffer[DATA_PACKET_BODY_INDEX].toInt() and 0xFF
            val longKeyState: Int =
                buffer[DATA_PACKET_BODY_INDEX + 1].toInt() and 0xFF
            Log.i(
                TAG,
                "KeyCode[" + keyCode + "] isLongKey[" + (if (longKeyState != 0) "true" else "false") + "]"
            )
            if (longKeyState == NORMAL_KEY) { /*短按键直接注入键值*/
                val now = System.currentTimeMillis()
                val down =
                    KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0)
                val up =
                    KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0)
                injectKeyEvent(down)
                injectKeyEvent(up)
            } else if (longKeyState == LONG_KEY_START) { /*长按键开始*/
                if (mLongKeyRunnable != null && mLongKeyRunnable!!.isRunning()) {
                    mLongKeyRunnable!!.stop()
                    mLongKeyRunnable = null
                }
                mLongKeyRunnable = LongKeyRunnable(keyCode)
                mPool.submit(mLongKeyRunnable)
            } else if (longKeyState == LONG_KEY_END) { /*长按键结束*/
                if (mLongKeyRunnable != null && mLongKeyRunnable!!.isRunning()) {
                    mLongKeyRunnable!!.stop()
                    mLongKeyRunnable = null
                }
            }
        } else if (load_type == STTP_LOAD_TYPE_BROADCAST && deviceId == 55) {
            try {
                val ip = datagramPacket.address.hostAddress
                val length: Int =
                    (buffer[8].toInt() and 0xFF or (buffer[9].toInt() and 0xFF shl 8)) - DATA_PACKET_TITLE_SIZE
                val deviceName = String(
                    buffer,
                    DATA_PACKET_BODY_INDEX,
                    length,
                    Charset.forName("utf-8")
                )
                if (!mIpDevMap.containsKey(ip)) {
                    if (deviceName != "") {
                        mIpDevMap[ip] = deviceName
                    }
                }
                Log.i(TAG, "REVEIVE BROADCAST FROM $deviceName[$ip]")
                /* 向手机端发送TV的IP,
                *  每次收到连接广播都会发连接请求,
                *  避免有时手机收不到连接请求的情况*/
                val sendBuffer: ByteArray = getByteBuffer(
                    STTP_LOAD_TYPE_REQUEST_CONNECTION,
                    0,
                    0
                )
                try {
                    val nameLength = Build.PRODUCT.toByteArray().size
                    val packetLength: Int =
                        nameLength + DATA_PACKET_TITLE_SIZE
                    sendBuffer[8] = Integer.valueOf(packetLength and 0xFF).toByte()
                    sendBuffer[9] =
                        Integer.valueOf(packetLength shr 8 and 0xFF).toByte()
                    val bip =
                        ByteArrayInputStream(Build.PRODUCT.toByteArray())
                    bip.read(
                        sendBuffer,
                        DATA_PACKET_BODY_INDEX,
                        nameLength
                    )
                    bip.close()
                    send(sendBuffer, nameLength, ip)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                Log.e(TAG, "error is $e")
            }
        } else if (load_type == STTP_LOAD_TYPE_CMD_INPUT_TEXT) {
            try {
                val length: Int =
                    (buffer[8].toInt() and 0xFF or (buffer[9].toInt() and 0xFF shl 8)) - DATA_PACKET_TITLE_SIZE
                val text = String(
                    buffer,
                    DATA_PACKET_BODY_INDEX,
                    length,
                    Charset.forName("utf-8")
                )
                Log.i(TAG, "receive Message text:$text")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
    }

    fun send(bf: ByteArray, length: Int, dstIp: String?) {
        if (dstIp == null) {
            Log.i(TAG, "dstIp is null")
            return
        }
        val sendRunnable = SendRunnable(bf, DATA_PACKET_TITLE_SIZE + length, dstIp)
        mPool.submit(sendRunnable)
    }

    private fun injectKeyEvent(event: KeyEvent) {
//        InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    fun getByteBuffer(load_type: Int, sn: Int, receive_flag: Int): ByteArray {
        var sn = sn
        val buffer = ByteArray(1400)

        /*初始化版本*/
        val version = 1 shl 6
        /*初始化设备ID*/
        val deviceId = 56 /*TV:56,Phone:55*/
        buffer[0] = Integer.valueOf(version or deviceId).toByte()
        buffer[1] =
            (Integer.valueOf(load_type and 0x7F) or if (receive_flag != 0) 0x80 else 0x00).toByte()

        /*SN:传输序列*/for (i in 2..3) {
            buffer[i] = Integer.valueOf(sn and 0xFF).toByte() /*取低八位*/
            sn = sn shr 8
        }
        buffer[4] =
            (if (mRandomCounter != 255) ++mRandomCounter else 255.let { mRandomCounter -= it; mRandomCounter }).toByte()
        return buffer
    }

    /**
     * Author:       LiMinsheng
     * Description:  发送数据
     */
    class SendRunnable(
        private val buffer: ByteArray,
        private val length: Int,
        private val dstIp: String
    ) : Runnable {

        companion object {
            private const val BROADCAST_PORT = 5556
        }

        override fun run() {
            try {
                val datagramPacket = DatagramPacket(
                    buffer, length,
                    InetAddress.getByName(dstIp), BROADCAST_PORT
                )
                if (mSendSocket == null || mSendSocket!!.isClosed) {
                    mSendSocket = DatagramSocket(null).run {
                        reuseAddress = true
                        bind(InetSocketAddress(BROADCAST_PORT))
                        this
                    }
                }
                mSendSocket?.send(datagramPacket)
                Log.i(TAG, "send message: " + (buffer[4].toInt() and 0xFF) + " To: " + dstIp)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                Log.w(TAG, "close SendRunnable socket")
                if (mSendSocket?.isClosed != true) {
                    mSendSocket?.close()
                }
            }
        }
    }

    /**
     * Author:       LiMinsheng
     * Description:  接收数据Runnable
     */
    class ReceiverRunnable : Runnable {

        companion object {
            const val BROADCAST_PORT = 5555
        }

        @Volatile
        var isFlag = true

        override fun run() {
            val receiverBuffer = ByteArray(DATA_PACKET_SIZE)
            val datagramPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
            try {
                if (mReceiverSocket == null || mReceiverSocket?.isClosed == true) {
                    mReceiverSocket = DatagramSocket(null).run {
                        reuseAddress = true
                        bind(InetSocketAddress(BROADCAST_PORT))
                        this
                    }
                }
                while (isFlag) {
                    Log.i(TAG, "----------enter loop and wait receive data----------")
                    mReceiverSocket?.receive(datagramPacket)
                    Log.e(TAG, "receive data from ------> ${datagramPacket.address.hostAddress}")
                    mPool.submit(ParseRunnable(receiverBuffer, datagramPacket))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (mReceiverSocket?.isClosed != true) {
                    mReceiverSocket?.close()
                }
            }
        }
    }

    /**
     * Author:       LiMinsheng
     * Description:  解析接收的数据Runnable
     */
    class ParseRunnable(private val data: ByteArray, private val datagramPacket: DatagramPacket) :
        Runnable {

        override fun run() {
            Log.i(TAG, "----------parse start----------")
            parseReceiveBuffer(data, datagramPacket)
            Log.i(TAG, "----------parse end----------")
        }
    }

    class LongKeyRunnable(private val keyCode: Int) : Runnable {

        @Volatile
        var isFlag = false

        fun stop() {
            isFlag = false
        }

        fun isRunning(): Boolean {
            return isFlag
        }

        override fun run() {
            isFlag = true
            while (isFlag) {
                kotlin.runCatching {
                    val now = System.currentTimeMillis()
                    val down = KeyEvent(
                        now,
                        now,
                        KeyEvent.ACTION_DOWN,
                        keyCode,
                        0
                    )
                    val up = KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0)
//                    InputManager.getInstance().injectInputEvent(down, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
//                    InputManager.getInstance().injectInputEvent(up, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
                    //                    InputManager.getInstance().injectInputEvent(down, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
//                    InputManager.getInstance().injectInputEvent(up, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
                    Log.i(TAG, "--------->injectKeyEvent $keyCode")
                    Thread.sleep(30)
                }
            }
            Log.i(TAG, "---------->stop LongKeyRunnable")
        }
    }
}