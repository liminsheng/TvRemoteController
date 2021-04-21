package com.minsheng.controller.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * ClassName:      RemoteReceiver
 * Author:         LiMinsheng
 * Date:           2021/4/20 16:44
 * Description:
 */
class RemoteReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DESTROY = "com.minsheng.controller.server.destroy"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == ACTION_DESTROY) {
            val startIntent = Intent("com.minsheng.controller.server")
            context.startService(startIntent)
        }
    }

}