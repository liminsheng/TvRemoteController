package com.minsheng.controller.server

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.minsheng.controller.server.util.NetUtil

/**
 * ClassName:      RemoteService
 * Author:         LiMinsheng
 * Date:           2021/4/20 16:37
 * Description:
 */
class RemoteService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        if (!NetUtil.isRunning()) {
            NetUtil.init()
        }

        val builder = Notification.Builder(this)
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(this, ServerActivity::class.java), 0
        )
        builder.setContentIntent(intent)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setTicker(javaClass.simpleName)
        builder.setContentTitle(javaClass.simpleName)
        builder.setContentText(javaClass.simpleName)
        val notification = builder.build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        val intent = Intent(RemoteReceiver.ACTION_DESTROY)
        sendBroadcast(intent)
    }

}