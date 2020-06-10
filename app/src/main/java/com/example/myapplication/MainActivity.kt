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
    private val setHomeBtn : Button
            get() = findViewById(R.id.setHomeBtn)
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
    private val KEY_IS_RECORDING = "is_recording"
    private val KEY_LOCATION_INFO_OBJECT = "location_object"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setHomeBtn.visibility = View.INVISIBLE
        if (savedInstanceState != null) {
            trackingBtn.text = savedInstanceState.getString(KEY_TRACK_TEXT)
            setHomeBtn.text = savedInstanceState.getString(KEY_SET_HOME_TEXT)
            locationTracker.isTracking = savedInstanceState.getBoolean(KEY_IS_RECORDING)
            if (locationTracker.isTracking) {
                locationTracker.startTracking()
            }
            val locationObjectAsJson: String? = savedInstanceState.getString(KEY_LOCATION_INFO_OBJECT)
            if (locationObjectAsJson != null) {
                val locationType = object : TypeToken<LocationInfo>(){}.type
                locationInfo = gson.fromJson(locationObjectAsJson, locationType)
                updateLocationView(textViewCurrLocation, locationInfo)
            }
        }

        setButtons()

        if (appContext.appSP.getHomeLocation() != null) {
            Log.d("getHomeLocation", "home accuracy = ${appContext.appSP.getHomeLocation()?.accuracy}")
            textViewHomeLocation.visibility = View.VISIBLE
            setHomeBtn.visibility = View.VISIBLE
            setHomeBtn.text = TEXT_DELETE_HOME
            updateLocationView(textViewHomeLocation, appContext.appSP.getHomeLocation())
        } else {
            textViewHomeLocation.visibility = View.INVISIBLE
            setHomeBtn.visibility = View.INVISIBLE
        }

        broadcastReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("updateLocation", "updated location")
                locationInfo = locationTracker.getLocationInfo()
                updateLocationView(textViewCurrLocation, locationInfo)
            }
        })
        LocalBroadcastManager.getInstance(appContext)
            .registerReceiver(broadcastReceiver, IntentFilter("update_location"))
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationView(textView: TextView, location: LocationInfo?){
        if (location != null){
            Log.d("updateLocationView", "isRecording = ${locationTracker.isTracking}")
            textView.text = "Accuracy = ${location.accuracy}\n" +
                    "Latitude = ${location.latitude}\n" +
                    "Longitude = ${location.longitude}"
        }

        if (locationInfo != null){
            when {
                locationInfo?.latitude == null -> {
                    textView.text = "Something went wrong.\nMake sure GPS is on."
                }
                locationInfo?.accuracy!! <= 50 -> {
                    setHomeBtn.visibility = View.VISIBLE
                }
                else -> {
                    setHomeBtn.visibility = View.INVISIBLE
                }
            }

        } else {
            textViewCurrLocation.text = TEXT_START_TRACKING
        }
    }

    private fun setButtons(){
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
                setHomeBtn.text = TEXT_DELETE_HOME
                updateLocationView(textViewHomeLocation, locationInfo)
            } else {
                Toast.makeText(applicationContext, "Start tracking first", Toast.LENGTH_LONG)
                    .show()
            }
        }

        setHomeBtn.setOnClickListener {
            when (setHomeBtn.text) {
                TEXT_SET_HOME -> {
                    if (locationInfo != null){
                        appContext.appSP.storeHomeLocation(locationInfo)
                        textViewHomeLocation.visibility = View.VISIBLE
                        setHomeBtn.text = TEXT_DELETE_HOME
                        updateLocationView(textViewHomeLocation, locationInfo)
                    } else {
                        Toast.makeText(applicationContext, "Start tracking first", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                TEXT_DELETE_HOME -> {
                    textViewHomeLocation.visibility = View.INVISIBLE
                    setHomeBtn.text = TEXT_SET_HOME
                    appContext.appSP.deleteHomeLocation()
                    if (locationInfo != null){
                        when {
                            locationInfo?.accuracy!! <= 50 -> {
                                setHomeBtn.visibility = View.VISIBLE
                            }
                            else -> setHomeBtn.visibility = View.INVISIBLE
                        }
                    } else {
                        setHomeBtn.visibility = View.INVISIBLE
                    }
                }
            }
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
        outState.putString(KEY_SET_HOME_TEXT, setHomeBtn.text as String?)
        outState.putBoolean(KEY_IS_RECORDING, locationTracker.isTracking)
        val locationObjectAsJson = gson.toJson(locationInfo)
        outState.putString(KEY_LOCATION_INFO_OBJECT, locationObjectAsJson)
    }
}
