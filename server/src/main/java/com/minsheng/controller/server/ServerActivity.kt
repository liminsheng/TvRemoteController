package com.minsheng.controller.server

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minsheng.controller.server.util.NetUtil

class ServerActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        startUdpServer()
    }

    private fun startUdpServer() {
        if (!NetUtil.isRunning()) {
            NetUtil.init()
        }
    }
}