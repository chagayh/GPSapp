package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LocationTracker(private var context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationInfo: LocationInfo? = null
    var isRecording: Boolean = false
    private val bgExecutor: Executor = Executors.newSingleThreadExecutor()

    private val LOG_TRACK = "trackingLog"

    fun startTracking() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            isRecording = true
            val runnable = Runnable {
                Log.d("threads", "curr thread = ${Thread.currentThread().name}")
                while (isRecording){

                    Thread.sleep(1000)
                    updateLocation()

                    val intent = Intent("update_location")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }
            }
            bgExecutor.execute(runnable)
        } else {
            Log.d(LOG_TRACK, "Permission not granted")
        }
    }

    private fun updateLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (locationInfo == null) {
                    locationInfo = LocationInfo(location?.accuracy,
                                                location?.latitude,
                                                location?.longitude)
                } else {
                    locationInfo?.accuracy  = location?.accuracy
                    locationInfo?.latitude  = location?.latitude
                    locationInfo?.longitude = location?.longitude
                }
                // Got last known location. In some rare situations this can be null:
                // null -> gps off, device recorded problem,
            }
    }

    fun stopTracking() {
        isRecording = false
        Log.d(LOG_TRACK, "stop tracking")
    }

    fun getLocationInfo(): LocationInfo? {
        return locationInfo
    }
}