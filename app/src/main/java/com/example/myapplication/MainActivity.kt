package com.example.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val locationTracker = LocationTracker(this)
    private val trackingBtn: Button = findViewById(R.id.trackingBtn)
    private val setHomeBtn : Button = findViewById(R.id.setHomeBtn)
    private val textViewHomeLocation: TextView = findViewById(R.id.textViewHomeLocation)
    private val textViewCurrLocation: TextView = findViewById(R.id.textViewCurrLocation)
    private val appContext: GPSapp = applicationContext as GPSapp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setButtons()

    }

    private fun setButtons(){
        trackingBtn.setOnClickListener {
            when (trackingBtn.text) {
                "Start tracking" -> {
                    locationTracker.startTracking()
                    trackingBtn.text = "Stop Tracking"
                }
                "Stop tracking" -> {
                    locationTracker.stopTracking()
                    trackingBtn.text = "Start Tracking"
                }
            }

        }

        setHomeBtn.setOnClickListener {
            when (setHomeBtn.text) {
                "Set location as home" -> {
                    // TODO - update the data to the sp.
                    textViewHomeLocation.visibility = View.VISIBLE
                    setHomeBtn.text = "Delete home location"
                }
                "Delete home location" -> {
                    // TODO - delete the data from the sp as well
                    textViewHomeLocation.visibility = View.INVISIBLE
                    setHomeBtn.text = "Set location as home"
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
