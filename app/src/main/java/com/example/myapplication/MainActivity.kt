package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val locationTracker = LocationTracker(this)
    private val trackingBtn: Button
            get() = findViewById(R.id.trackingBtn)
    private val deleteHomeBtn : Button
            get() = findViewById(R.id.deleteHomeBtn)
    private val fixSetHomeBtn : Button
        get() = findViewById(R.id.setTopBtn)
    private val textViewHomeLocation: TextView
            get() = findViewById(R.id.textViewHomeLocation)
    private val textViewCurrLocation: TextView
            get() = findViewById(R.id.textViewCurrLocation)
    private val appContext: GPSapp
            get() = applicationContext as GPSapp
    private var locationInfo: LocationInfo? = null
    private val gson: Gson = Gson()
    lateinit var broadcastReceiver: BroadcastReceiver

    private val REQUEST_CODE_PERMISSION_GPS = 1234
    private val TEXT_SET_HOME = "Set location as home"
    private val TEXT_DELETE_HOME = "Delete home location"
    private val TEXT_START_TRACKING = "Start Tracking"
    private val TEXT_STOP_TRACKING = "Stop Tracking"
    private val LOG_PERMISSION = "permission"
    private val KEY_TRACK_TEXT = "track_btn_text"
    private val KEY_SET_HOME_TEXT = "set_home_btn_text"
    private val KEY_IS_TRACKING = "is_recording"
    private val KEY_LOCATION_INFO_OBJECT = "location_object"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadDataFromBundle(savedInstanceState)

        setButtons()

        loadHomeLocationFromSP()

        setBroadcast()
    }

    private fun loadHomeLocationFromSP() {
        if (appContext.appSP.getHomeLocation() != null) {
            textViewHomeLocation.visibility = View.VISIBLE
            fixSetHomeBtn.visibility = if (locationInfo == null) View.INVISIBLE else View.VISIBLE
            updateHomeLocationView(appContext.appSP.getHomeLocation())
        } else {
            textViewHomeLocation.visibility = View.INVISIBLE
            fixSetHomeBtn.visibility = View.INVISIBLE
        }
    }

    private fun loadDataFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            trackingBtn.text = savedInstanceState.getString(KEY_TRACK_TEXT)
            locationTracker.isTracking = savedInstanceState.getBoolean(KEY_IS_TRACKING)
            if (locationTracker.isTracking) {
                locationTracker.startTracking()
            }
            val locationObjectAsJson: String? = savedInstanceState.getString(KEY_LOCATION_INFO_OBJECT)
            if (locationObjectAsJson != null) {
                val locationType = object : TypeToken<LocationInfo>(){}.type
                locationInfo = gson.fromJson(locationObjectAsJson, locationType)
                updateCurrLocationView()
            }
        }
    }

    private fun setBroadcast() {
        broadcastReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("updateLocation", "updated location")
                locationInfo = locationTracker.getLocationInfo()
                updateCurrLocationView()
            }
        })
        LocalBroadcastManager.getInstance(appContext)
            .registerReceiver(broadcastReceiver, IntentFilter("update_location"))
    }

    @SuppressLint("SetTextI18n")
    private fun updateHomeLocationView(location: LocationInfo?){
        if (location != null){
            Log.d("updateLocationView", "isRecording = ${locationTracker.isTracking}")
            textViewHomeLocation.text = "Your home location is defined as\n<${location.latitude}, " +
                    "${location.longitude}>"
            deleteHomeBtn.visibility =  View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrLocationView(){
        if (locationInfo != null && locationInfo?.latitude != null){
            Log.d("updateLocationView", "isRecording = ${locationTracker.isTracking}")
            textViewCurrLocation.text = "Accuracy = ${locationInfo?.accuracy}\n" +
                    "Latitude = ${locationInfo?.latitude}\n" +
                    "Longitude = ${locationInfo?.longitude}"

            fixSetHomeBtn.visibility = if (locationInfo?.accuracy!! <= 50) View.VISIBLE
                                       else View.INVISIBLE
        } else {
            textViewCurrLocation.text = TEXT_START_TRACKING
        }
    }

    private fun setButtons(){
        deleteHomeBtn.visibility = View.INVISIBLE
        if (locationInfo != null){
            fixSetHomeBtn.visibility = if (locationInfo?.accuracy!! <= 50) View.VISIBLE
                                       else View.INVISIBLE
        }

        trackingBtn.setOnClickListener {
            when {
                isPermissionGranted() -> {
                    when (trackingBtn.text) {
                        TEXT_START_TRACKING -> {
                            trackingBtn.text = TEXT_STOP_TRACKING
                            locationTracker.startTracking()
                        }
                        TEXT_STOP_TRACKING -> {
                            trackingBtn.text = TEXT_START_TRACKING
                            locationTracker.stopTracking()
                        }
                    }
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_CODE_PERMISSION_GPS)
                }
            }
        }

        fixSetHomeBtn.setOnClickListener {
            if (locationInfo != null){
                appContext.appSP.deleteHomeLocation()
                appContext.appSP.storeHomeLocation(locationInfo)
                textViewHomeLocation.visibility = View.VISIBLE
                updateHomeLocationView(locationInfo)
            } else {
                Toast.makeText(applicationContext, "Start tracking first", Toast.LENGTH_LONG)
                    .show()
            }
        }

        deleteHomeBtn.setOnClickListener {
            textViewHomeLocation.visibility = View.INVISIBLE
            deleteHomeBtn.visibility = View.INVISIBLE
            appContext.appSP.deleteHomeLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_GPS -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(LOG_PERMISSION, "Asked more than once")
                        // reached here? means we asked the user for this permission more than once,
                        // and they still refuse. This would be a good time to open up a dialog
                        // explaining why we need this permission
                        Toast.makeText(applicationContext
                            ,"We need your GPS permission in order to start tracking"
                            ,Toast.LENGTH_LONG)
                            .show()
                    }
                    Log.d(LOG_PERMISSION, "Permission has been denied by user")
                } else {
                    trackingBtn.text = TEXT_STOP_TRACKING
                    locationTracker.startTracking()
                    Log.d(LOG_PERMISSION, "Permission has been granted by user")
                }
            }
        }
    }

    private fun isPermissionGranted(): Boolean{
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopTracking()
        locationTracker.shoutDownExecutor()
        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TRACK_TEXT, trackingBtn.text as String?)
        outState.putBoolean(KEY_IS_TRACKING, locationTracker.isTracking)
        val locationObjectAsJson = gson.toJson(locationInfo)
        outState.putString(KEY_LOCATION_INFO_OBJECT, locationObjectAsJson)
    }
}
