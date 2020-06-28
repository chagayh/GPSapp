package com.example.myapplication

import android.app.Application
import android.content.IntentFilter
import androidx.work.*
import java.util.concurrent.TimeUnit

class GPSapp: Application() {
    lateinit var appSP: AppSP
    lateinit var notificationFireHelper: NotificationFireHelper

    override fun onCreate(){
        super.onCreate()
        appSP = AppSP(this)
        notificationFireHelper = NotificationFireHelper(this)

        val periodicWorkRequestBuilder = PeriodicWorkRequest.Builder(CustomAsyncWorker::class.java,
            15,
            TimeUnit.MINUTES)
            .setConstraints(Constraints.NONE)
            .build()

        val broadcastSendSmsReceiver = LocalSendSmsBroadcastReceiver(this, notificationFireHelper)

        registerReceiver(broadcastSendSmsReceiver, IntentFilter("POST_PC.ACTION_SEND_SMS"))
//        LocalBroadcastManager.getInstance(this)
//            .registerReceiver(broadcastSendSmsReceiver, IntentFilter("POST_PC.ACTION_SEND_SMS"))

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork("location",
                                                ExistingPeriodicWorkPolicy.REPLACE,
                                                periodicWorkRequestBuilder)

    }
}