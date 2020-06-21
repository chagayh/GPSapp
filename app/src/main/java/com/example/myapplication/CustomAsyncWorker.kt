package com.example.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*
import com.google.common.util.concurrent.ListenableFuture

// when receiving this data, the worker finishes and passing the broadcast as a result
// it works ASYNC, meaning that the method startWork() should only return a Future.
class CustomAsyncWorker(private val context: Context, workerParams: WorkerParameters):
    ListenableWorker(context, workerParams) {
    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private var receiver: BroadcastReceiver? = null
    private var lastLocation: LocationInfo? = null
    private var currLocation: LocationInfo? = null
    private var homeLocation: LocationInfo? = null
    private val fusedLocationClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationCallback: LocationCallback
    private val appContext: GPSapp
        get() = applicationContext as GPSapp


    override fun startWork(): ListenableFuture<Result> {
        // 1. here we create the future and store the callback for later use
        val future = CallbackToFutureAdapter.getFuture {callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback
            return@getFuture null
        }

        // we place the broadcast receiver and immediately return the "future" object
        doWork()
        return future
    }

    private fun doWork() {
        Log.d("CustomAsyncWorker", "In Do Work of custom worker")
        Log.d("CustomAsyncWorker", "homeLocation = ${appContext.appSP.getHomeLocation()}")
        appContext.notificationFireHelper.fireNotification("started working in BG")

        if (!hasPermissions() || !hasStoredHomePhoneData()){
            Log.d("CustomAsyncWorker", "first if")
            this.callback?.set(Result.success())
        }

        if (isGpsOff()){
            Log.d("CustomAsyncWorker", "second if")
            this.callback?.set(Result.success())
        }

        startTracking()
        lastLocation = appContext.appSP.getLastLocation()

        if (lastLocation == null || isCloseEnough(lastLocation)){
            Log.d("CustomAsyncWorker", "third if")
            appContext.notificationFireHelper.fireNotification("third")
            appContext.appSP.storeLastLocation(currLocation)
        } else {
            when {
                isCloseEnough(homeLocation) -> {
                    val intent = Intent("POST_PC.ACTION_SEND_SMS")
                    intent.putExtra("PHONE", appContext.appSP.getPhoneNumber())
                    intent.putExtra("CONTENT",  "Honey I'm Home!")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }
                else -> {
                    appContext.appSP.storeLastLocation(currLocation)
                }
            }
        }

        this.callback?.set(Result.success())
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
        Log.d("CustomAsyncWorker", "stop tracking")
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

    // 2. we place the broadcast receiver now, waiting for it to fire in the future
    private fun placeReceiver(){
        // create the broadcast object and register it:

        this.receiver = object : BroadcastReceiver() {
            // notice that the fun onReceive() will get called in the future, not now
            override fun onReceive(context: Context?, intent: Intent?) {
                // got broadcast!
                onReceivedBroadcast()
            }
        }

        this.applicationContext.registerReceiver(this.receiver, IntentFilter("my_data_broadcast"))
    }

    // 3. when the broadcast receiver fired, we finished the work!
    // so we will clean all and call the callback to tell WorkManager that we are DONE
    private fun onReceivedBroadcast(){
        this.applicationContext.unregisterReceiver(this.receiver)

        this.callback?.set(Result.success())
    }
}