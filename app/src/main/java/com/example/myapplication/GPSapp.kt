package com.example.myapplication

import android.app.Application
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.stream.DoubleStream.builder

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

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork("location",
                                                ExistingPeriodicWorkPolicy.KEEP,
                                                periodicWorkRequestBuilder)

        val broadcastSendSmsReceiver = LocalSendSmsBroadcastReceiver(this, notificationFireHelper)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastSendSmsReceiver, IntentFilter("POST_PC.ACTION_SEND_SMS"))
    }

//    @Test
//    @Throws(Exception::class)
//    fun testPeriodicWork() {
//        // Define input data
//
//        // Create request
//        val request = PeriodicWorkRequestBuilder<EchoWorker>(15, TimeUnit.MINUTES)
//            .build()
//
//        val workManager = WorkManager.getInstance(this)
//        val testDriver = WorkManagerTestInitHelper.getTestDriver()
//        // Enqueue and wait for result.
//        workManager.enqueue(request).result.get()
//        // Tells the testing framework the period delay is met
//        testDriver.setPeriodDelayMet(request.id)
//        // Get WorkInfo and outputData
//        val workInfo = workManager.getWorkInfoById(request.id).get()
//        // Assert
//        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
//    }

}