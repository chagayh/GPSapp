package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocationTracker(private var context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationInfo: LocationInfo? = null
    var isTracking: Boolean = false
    private val bgExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val LOG_TRACK = "trackingLog"

    fun startTracking() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            isTracking = true
            val runnable = Runnable {
                while (isTracking){
                    Log.d(LOG_TRACK, "tracking")
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
                if (location == null) {
                    Toast.makeText(context,
                        "Something went wrong.\nMake sure your GPS is on",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    fun stopTracking() {
        isTracking = false
        Log.d(LOG_TRACK, "stop tracking")
        // TODO - add a broadcast that tracking stopped
    }

    fun shoutDownExecutor() {
        bgExecutor.shutdown()
    }

    fun getLocationInfo(): LocationInfo? {
        return locationInfo
    }
}