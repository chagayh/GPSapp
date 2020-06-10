package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val locationTracker = LocationTracker(this)
    private var trackingBtn: Button? = null
    private var setHomeBtn : Button? = null
    private var textViewHomeLocation: TextView? = null
    private var textViewCurrLocation: TextView? = null
    private var appContext: GPSapp? = null

    private val REQUEST_CODE_PERMISSION_GPS = 1234
    private val TEXT_SET_HOME = "Set location as home"
    private val TEXT_DELETE_HOME = "Delete home location"
    private val TEXT_START_TRACKING = "Start Tracking"
    private val TEXT_STOP_TRACKING = "Stop Tracking"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        trackingBtn = findViewById(R.id.trackingBtn)
        setHomeBtn  = findViewById(R.id.setHomeBtn)
        textViewHomeLocation = findViewById(R.id.textViewHomeLocation)
        textViewCurrLocation = findViewById(R.id.textViewCurrLocation)
        appContext = applicationContext as GPSapp
//        setButtons()
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

    private fun isPermissionGranted(): Boolean{
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    // TODO - save the last know location when exit the activity (flip the screen)
    // TODO - broadcast
}
