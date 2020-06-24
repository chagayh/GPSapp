package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*

// example: this worker calculates the result of contacting multiple strings
// it runs synchronously - the method doWork() returns the result
class CustomSyncWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private var lastLocation: LocationInfo? = null
    private var currLocation: LocationInfo? = null
    private var homeLocation: LocationInfo? = null
    private val fusedLocationClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationCallback: LocationCallback
    private val appContext: GPSapp
            get() = applicationContext as GPSapp

    override fun doWork(): Result {
        Log.d("startJob", "In Do Work of custom worker")
        Log.d("startJob", "homeLocation = ${appContext.appSP.getHomeLocation()}")
        appContext.notificationFireHelper.fireNotification("started working in BG")

//        if (!hasPermissions() || !hasStoredHomePhoneData()){
//            Log.d("startJob", "first if")
//            return Result.success()
//        }
//
//        if (isGpsOff()){
//            Log.d("startJob", "second if")
//            return Result.success()
//        }
//
//        startTracking()
//        lastLocation = appContext.appSP.getLastLocation()
//
//        if (lastLocation == null || isCloseEnough(lastLocation)){
//            Log.d("startJob", "third if")
//            appContext.notificationFireHelper.fireNotification("third")
//            appContext.appSP.storeLastLocation(currLocation)
//        } else {
//            when {
//                isCloseEnough(homeLocation) -> {
//                    val intent = Intent("POST_PC.ACTION_SEND_SMS")
//                    intent.putExtra("PHONE", appContext.appSP.getPhoneNumber())
//                    intent.putExtra("CONTENT",  "Honey I'm Home!")
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                }
//                else -> {
//                    appContext.appSP.storeLastLocation(currLocation)
//                }
//            }
//        }
        return Result.success()
    }

    private fun isGpsOff(): Boolean{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            return true
        }
        return false
    }

    private fun startTracking(){
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // do work here
                val current = locationResult.lastLocation
                if (current.accuracy < 50){
                    currLocation = LocationInfo(current.accuracy, current.latitude, current.longitude)
                    stopTracking()
                }
            }
        }

        // Create the location request to start receiving updates
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(context)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.myLooper())
    }

    private fun stopTracking(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("customWorker", "stop tracking")
    }

    private fun isCloseEnough(location: LocationInfo?): Boolean{
        val results = FloatArray(5) // TODO - check
        Location.distanceBetween(
            location?.latitude!!,
            location.longitude!!,
            currLocation?.latitude!!,
            currLocation?.longitude!!,
            results
        )
        if (results[0] < 50) {
            return true
        }
        return false
    }

    private fun hasPermissions(): Boolean{
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
    }

    private fun hasStoredHomePhoneData(): Boolean{
        homeLocation = appContext.appSP.getHomeLocation()
        if (homeLocation == null || appContext.appSP.getPhoneNumber() == null)
        {
            return false
        }
        return true
    }
}
