package com.example.myapplication

import android.app.Application

class GPSapp: Application() {
    lateinit var appSP: AppSP
    lateinit var notificationFireHelper: NotificationFireHelper

    override fun onCreate(){
        super.onCreate()
        appSP = AppSP(this)
        notificationFireHelper = NotificationFireHelper(this)
    }
}