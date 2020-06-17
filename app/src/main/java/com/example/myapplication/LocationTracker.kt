package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocationTracker(private val context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationInfo: LocationInfo? = null
    var isTracking: Boolean = false
    private val bgExecutor: ExecutorService = Executors.newSingleThreadExecutor()
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
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }


//        locationListener = object : LocationListener {
//            override fun onLocationChanged(location: Location?) {
////                updateLocation()
//
//                if (location != null) {
//                    locationInfo = LocationInfo(location.accuracy, location.latitude, location.longitude)
//                    val intent = Intent("update_location")
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                } else {
//                    Log.d("locationChanged", "location = null")
//                }
//            }
//
//            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onProviderEnabled(provider: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onProviderDisabled(provider: String?) {
//                buildDialog()
//                Toast.makeText(context,
//                "Make sure your GPS is on",
//                Toast.LENGTH_SHORT)
//                .show()
//            }
//        }
    }


    private fun buildAlertMessageNoGps(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->   // TODO
//                startActivityForResult(
//                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                    , 11)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
            }
        val alert: AlertDialog  = builder.create()
        alert.show()
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
                buildAlertMessageNoGps()
            } else {
                isTracking = true
                startLocationUpdates()
                val intent = Intent("start_tracking")
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }
 
    fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(LOG_TRACK, "stop tracking")
        val intent = Intent("stop_tracking")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        // TODO - add a broadcast that tracking stopped
    }

    fun shoutDownExecutor() {
        bgExecutor.shutdown()
    }

    fun getLocationInfo(): LocationInfo? {
        return locationInfo
    }
}