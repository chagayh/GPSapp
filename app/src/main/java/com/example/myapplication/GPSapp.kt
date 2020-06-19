package com.example.myapplication

import android.app.Application
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.stream.DoubleStream.builder

class GPSapp: Application() {
    lateinit var appSP: AppSP
    lateinit var notificationFireHelper: NotificationFireHelper

    override fun onCreate(){
        super.onCreate()
        appSP = AppSP(this)
        notificationFireHelper = NotificationFireHelper(this)

        val request = PeriodicWorkRequest.Builder(
            CustomAsyncWorker::class.java,
            15,
            TimeUnit.MINUTES)
//            .setConstraints(Constraints.)
            .build()
        val workManager = WorkManager.getInstance(this)

        val broadcastSendSmsReceiver  = LocalSendSmsBroadcastReceiver(this, notificationFireHelper)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastSendSmsReceiver, IntentFilter("POST_PC.ACTION_SEND_SMS"))
    }
}