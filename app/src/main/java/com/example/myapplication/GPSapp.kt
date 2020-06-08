package com.example.myapplication

import android.app.Application

class GPSapp: Application() {
    val appSP: AppSP = AppSP(this)
}