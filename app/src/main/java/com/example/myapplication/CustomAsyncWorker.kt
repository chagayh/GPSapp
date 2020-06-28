package com.example.myapplication

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
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
    private val locationTracker: LocationTracker = LocationTracker(context)
    private val appContext: GPSapp
        get() = applicationContext as GPSapp


    override fun startWork(): ListenableFuture<Result> {
        // 1. here we create the future and store the callback for later use
        val future = CallbackToFutureAdapter.getFuture {callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback

            return@getFuture null
        }

        Log.d("asyncWorker", "start")

        if (!hasPermissions() || !hasStoredHomePhoneData()){
            Log.d("asyncWorker", "permission or data prob")
            this.callback?.set(Result.success())
        }

        else if (isGpsOff()){
            Log.d("asyncWorker", "gps off")
            this.callback?.set(Result.success())
        }

        else {
            placeReceiver()

            Log.d("asyncWorker", "after place receiver")
//        appContext.notificationFireHelper.fireNotification("start")     // To see if gets here

            locationTracker.startTracking()
        }

        return future
    }

    private fun isGpsOff(): Boolean{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            return true
        }
        return false
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
                Log.d("asyncWorker", "in onReceive")

                // got broadcast! - there is a new location
                lastLocation = appContext.appSP.getLastLocation()
                currLocation = locationTracker.getLocationInfo()    // the new location

                Log.d("asyncWorker", "after last and curr location")

                if (lastLocation == null || isCloseEnough(lastLocation)){
                    Log.d("CustomAsyncWorker", "last = null || close enough from last loc")
                    appContext.appSP.storeLastLocation(currLocation)
                } else {
                    if (isCloseEnough(homeLocation))  {
                        // Send sms broadcast
                            val phoneIntent = Intent("POST_PC.ACTION_SEND_SMS")
                            phoneIntent.putExtra("PHONE", appContext.appSP.getPhoneNumber())
                            phoneIntent.putExtra("CONTENT",  "Honey I'm Home!")
                            context?.sendBroadcast(phoneIntent)
//                            LocalBroadcastManager.getInstance(context).sendBroadcast(phoneIntent)
                    }
                    appContext.appSP.storeLastLocation(currLocation)
                }

                onReceivedBroadcast()
            }
        }
        this.context.registerReceiver(this.receiver, IntentFilter("update_location"))
    }

    // 3. when the broadcast receiver fired, we finished the work!
    // so we will clean all and call the callback to tell WorkManager that we are DONE
    private fun onReceivedBroadcast(){
        this.context.unregisterReceiver(this.receiver)
        locationTracker.stopTracking()

        receiver = null
        lastLocation = null
        currLocation = null
        homeLocation = null

        this.callback?.set(Result.success())
    }
}