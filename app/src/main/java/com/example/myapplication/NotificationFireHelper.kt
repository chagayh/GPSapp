package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationFireHelper(private val context: Context) {

    private val channelId = "CHANNEL_ID_FOR_HOME_NOTIFICATIONS"

    fun fireNotification(msg: String){
        createChannelIfNotExists()
        actualFire(msg)
    }


    private fun createChannelIfNotExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notificationChannels.forEach { channel ->
                if (channel.id == channelId) {
                    return
                }
            }

            // Create the NotificationChannel
            val name = "home-notification"
            val descriptionText = "channel for got home notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun actualFire(msg: String) {

        val notification: Notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.home)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()


        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.notify(123, notification)

//
//        val runnable4 = Runnable {
//            val notification2: Notification = NotificationCompat.Builder(context, channelId)
//                    .setSmallIcon(R.drawable.man)
//                    .setContentText("I was changed!")
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .build()
//            notificationManager.notify(123, notification2)
//        }
//        Handler().postDelayed(runnable4, 4000)
//
//
//        val runnable8 = Runnable {
//            notificationManager.cancel(123)
//        }
//        Handler().postDelayed(runnable8, 8000)
//
//


    }
}