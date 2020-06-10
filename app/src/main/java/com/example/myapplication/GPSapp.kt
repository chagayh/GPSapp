package com.example.myapplication

import android.app.Application

class GPSapp: Application() {
    lateinit var appSP: AppSP

    override fun onCreate(){
        super.onCreate()
        appSP = AppSP(this)
    }
}