package com.example.myapplication

import android.app.Application
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.security.AccessController.getContext

class GPSapp: Application() {
    lateinit var appSP: AppSP
    lateinit var notificationFireHelper: NotificationFireHelper

    override fun onCreate(){
        super.onCreate()
        appSP = AppSP(this)
        notificationFireHelper = NotificationFireHelper(this)

        val broadcastSendSmsReceiver  = LocalSendSmsBroadcastReceiver(this, notificationFireHelper)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastSendSmsReceiver, IntentFilter("POST_PC.ACTION_SEND_SMS"))
    }
}