package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationTracker(private val context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationInfo: LocationInfo? = null
    var isTracking: Boolean = false
    private var locationCallback: LocationCallback

    private val LOG_TRACK = "trackingLog"

    init {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // do work here
                locationInfo = LocationInfo(
                    locationResult.lastLocation.accuracy,
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )
                val intent = Intent("update_location")
                context.sendBroadcast(intent)
                Log.d("locationTracker", "after send update broadcast")
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }

    private fun startLocationUpdates() {

        // Create the location request to start receiving updates
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(context)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.myLooper())
    }

    fun startTracking() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
//                buildAlertMessageNoGps()
                val intent = Intent("gps_off")
                context.sendBroadcast(intent)
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            } else {
                isTracking = true
                startLocationUpdates()
                val intent = Intent("start_tracking")
                context.sendBroadcast(intent)
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }

    fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(LOG_TRACK, "stop tracking")
        val intent = Intent("stop_tracking")
        context.sendBroadcast(intent)
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun getLocationInfo(): LocationInfo? {
        return locationInfo
    }
}