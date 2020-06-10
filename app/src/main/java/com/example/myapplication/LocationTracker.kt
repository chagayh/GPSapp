package com.example.myapplication

import android.content.Context
import android.util.Log

class LocationTracker (private var context: Context) {
    private val LOG_TRACK = "TreackingLog"

    fun startTracking() {
        Log.d(LOG_TRACK, "start tracking")
    }

    fun stopTracking() {
        Log.d(LOG_TRACK, "stop tracking")
    }
}