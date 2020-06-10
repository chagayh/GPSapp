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

class LocationTracker (private var context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationInfo: LocationInfo = LocationInfo()
    var isRecording: Boolean = false

    private val LOG_TRACK = "trackingLog"

    fun startTracking() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TRACK, "start tracking")

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    locationInfo = LocationInfo(location?.accuracy,
                                                location?.latitude,
                                                location?.longitude)
                    Log.d(LOG_TRACK, "accuracy = ${locationInfo.accuracy}, " +
                            "latitude = ${locationInfo.latitude}," +
                            "longitude = ${locationInfo.longitude}")
                    // Got last known location. In some rare situations this can be null.
                    // null -> gps off, device recorded problem,
                    val intent = Intent("update_location")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }
        }
        else {
            Log.d(LOG_TRACK, "Permission not granted")
        }
    }

    fun stopTracking() {
        Log.d(LOG_TRACK, "stop tracking")
    }

    fun getLocationInfo(): LocationInfo{
        return locationInfo
    }
}