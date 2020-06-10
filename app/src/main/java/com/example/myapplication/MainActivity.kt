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
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val locationTracker = LocationTracker(this)
    private val trackingBtn: Button
            get() = findViewById(R.id.trackingBtn)
    private val setHomeBtn : Button
            get() = findViewById(R.id.setHomeBtn)
    private val textViewHomeLocation: TextView
            get() = findViewById(R.id.textViewHomeLocation)
    private val textViewCurrLocation: TextView
            get() = findViewById(R.id.textViewCurrLocation)
    private val appContext: GPSapp
            get() = applicationContext as GPSapp
    private var locationInfo: LocationInfo = LocationInfo()
    private val gson: Gson = Gson()

    private val REQUEST_CODE_PERMISSION_GPS = 1234
    private val TEXT_SET_HOME = "Set location as home"
    private val TEXT_DELETE_HOME = "Delete home location"
    private val TEXT_START_TRACKING = "Start Tracking"
    private val TEXT_STOP_TRACKING = "Stop Tracking"
    private val LOG_PERMISSION = "permission"
    private val KEY_TRACK_TEXT = "track_btn_text"
    private val KEY_SET_HOME_TEXT = "set_home_btn_text"
    private val KEY_HOME_LOCATION_VISIBILITY = "home_location_visibility"
    private val KEY_IS_RECORDING = "is_recording"
    private val KEY_LOCATION_INFO_OBJECT = "location_object"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewHomeLocation.visibility = View.INVISIBLE
        if (savedInstanceState != null) {
            trackingBtn.text = savedInstanceState.getString(KEY_TRACK_TEXT)
            setHomeBtn.text = savedInstanceState.getString(KEY_SET_HOME_TEXT)
            textViewHomeLocation.visibility = savedInstanceState.getInt(KEY_HOME_LOCATION_VISIBILITY)
            locationTracker.isRecording = savedInstanceState.getBoolean(KEY_IS_RECORDING)
            val locationObjectAsJson: String? = savedInstanceState.getString(KEY_LOCATION_INFO_OBJECT)
            if (locationObjectAsJson != null) {
                val locationType = object : TypeToken<LocationInfo>(){}.type
                locationInfo = gson.fromJson(locationObjectAsJson, locationType)
            }
            setLocationView()
        }
        setButtons()
        val broadcastReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("updateLocation", "updated location")
                locationInfo = locationTracker.getLocationInfo()
                setLocationView()
            }
        })
        LocalBroadcastManager.getInstance(appContext)
            .registerReceiver(broadcastReceiver, IntentFilter("update_location"))
    }

    @SuppressLint("SetTextI18n")
    private fun setLocationView(){
        textViewCurrLocation.text = "Accuracy = ${locationInfo.accuracy}\n" +
                "Latitude = ${locationInfo.latitude}\n" +
                "Longitude = ${locationInfo.longitude}"
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

        setHomeBtn.setOnClickListener {
            // TODO - in case pressed before granted gps, and nothing to show
            when (setHomeBtn.text) {
                TEXT_SET_HOME -> {
                    // TODO - update the data to the sp.
                    textViewHomeLocation.visibility = View.VISIBLE
                    setHomeBtn.text = TEXT_DELETE_HOME
                }
                TEXT_DELETE_HOME -> {
                    // TODO - delete the data from the sp as well
                    textViewHomeLocation.visibility = View.INVISIBLE
                    setHomeBtn.text = TEXT_SET_HOME
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
                    // TODO - retrieve data

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

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TRACK_TEXT, trackingBtn.text as String?)
        outState.putString(KEY_SET_HOME_TEXT, setHomeBtn.text as String?)
        outState.putInt(KEY_HOME_LOCATION_VISIBILITY, textViewHomeLocation.visibility)
        outState.putBoolean(KEY_IS_RECORDING, locationTracker.isRecording)
        val locationObjectAsJson = gson.toJson(locationInfo)
        outState.putString(KEY_LOCATION_INFO_OBJECT, locationObjectAsJson)
    }

    // TODO - save the last know location when exit the activity (flip the screen)
    // TODO - unregister broadcast
    // TODO - save the text of the buttons when flipping the phone
}
