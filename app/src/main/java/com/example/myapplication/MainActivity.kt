package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val locationTracker = LocationTracker(this)
    private val trackingBtn: Button
        get() = findViewById(R.id.trackingBtn)
    private val deleteHomeBtn: Button
        get() = findViewById(R.id.deleteHomeBtn)
    private val fixSetHomeBtn: Button
        get() = findViewById(R.id.setTopBtn)
    private val testSmsBtn: Button
        get() = findViewById(R.id.testSMSBtn)
    private val setPhoneNumBtn: Button
        get() = findViewById(R.id.setPhoneNumBtn)
    private val deletePhoneNumBtn: Button
        get() = findViewById(R.id.deletePhoneNumBtn)
    private val textViewHomeLocation: TextView
        get() = findViewById(R.id.textViewHomeLocation)
    private val textViewCurrLocation: TextView
        get() = findViewById(R.id.textViewCurrLocation)
    private val appContext: GPSapp
        get() = applicationContext as GPSapp
    private var locationInfo: LocationInfo? = null
    private val gson: Gson = Gson()
    lateinit var broadcastLocationReceiver: BroadcastReceiver
    lateinit var broadcastStartTrackingReceiver: BroadcastReceiver
    lateinit var broadcastStopTrackingReceiver: BroadcastReceiver
    lateinit var broadcastGpsOffReceiver: BroadcastReceiver


    private val REQUEST_CODE_PERMISSION_GPS = 1234
    private val REQUEST_CODE_PERMISSION_SMS = 1
    private val TEXT_START_TRACKING = "Start Tracking"
    private val TEXT_STOP_TRACKING = "Stop Tracking"
    private val LOG_PERMISSION = "permission"
    private val KEY_TRACK_TEXT = "track_btn_text"
    private val KEY_IS_TRACKING = "is_recording"
    private val KEY_LOCATION_INFO_OBJECT = "location_object"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadDataFromBundle(savedInstanceState)

        setButtons()

        loadDataFromSP()

        setBroadcast()
    }

    private fun loadDataFromSP() {
        if (appContext.appSP.getPhoneNumber() != null) {
            testSmsBtn.visibility = View.VISIBLE
            deletePhoneNumBtn.visibility = View.VISIBLE
        }

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
            val locationObjectAsJson: String? =
                savedInstanceState.getString(KEY_LOCATION_INFO_OBJECT)
            if (locationObjectAsJson != null) {
                val locationType = object : TypeToken<LocationInfo>() {}.type
                locationInfo = gson.fromJson(locationObjectAsJson, locationType)
                updateCurrLocationView()
            }
        }
    }

    private fun setBroadcast() {

        // Update locaion broadcast
        broadcastLocationReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("updateLocation", "updated location")
                locationInfo = locationTracker.getLocationInfo()

                when (val lastLocation = appContext.appSP.getLastLocation()) {
                    null -> appContext.appSP.storeLastLocation(locationInfo)
                    else -> {
                        val results = FloatArray(5)
                        Location.distanceBetween(
                            locationInfo?.latitude!!,
                            locationInfo!!.longitude!!,
                            lastLocation.latitude!!,
                            lastLocation.longitude!!,
                            results
                        )
                        if (results[0] > 50) {
                            appContext.appSP.storeLastLocation(locationInfo)
                        }
                    }
                }
                updateCurrLocationView()
            }
        })
//        registerReceiver(broadcastLocationReceiver, IntentFilter("update_location"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastLocationReceiver, IntentFilter("update_location"))

        // start tracking broadcast
        broadcastStartTrackingReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("start_tracking", "start tracking")
                locationInfo = locationTracker.getLocationInfo()
                Toast.makeText(applicationContext, "Start tracking", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        registerReceiver(broadcastStartTrackingReceiver, IntentFilter("start_tracking"))
//        LocalBroadcastManager.getInstance(appContext)
//            .registerReceiver(broadcastStartTrackingReceiver, IntentFilter("start_tracking"))

        // Gps off broadcast
        broadcastGpsOffReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("gps_off", "gps is off")
//                buildAlertMessageNoGps()  // TODO - not working
                Toast.makeText(context, "Make sure GPS is on", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        registerReceiver(broadcastGpsOffReceiver, IntentFilter("gps_off"))
//        LocalBroadcastManager.getInstance(appContext)
//            .registerReceiver(broadcastGpsOffReceiver, IntentFilter("gps_off"))

        // Stopped tracking broadcast
        broadcastStopTrackingReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("stop_tracking", "stopped tracking")
                Toast.makeText(applicationContext, "Stopped tracking", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        registerReceiver(broadcastStopTrackingReceiver, IntentFilter("stop_tracking"))
//        LocalBroadcastManager.getInstance(appContext)
//            .registerReceiver(broadcastStopTrackingReceiver, IntentFilter("stop_tracking"))
    }

    @SuppressLint("SetTextI18n")
    private fun updateHomeLocationView(location: LocationInfo?) {
        if (location != null) {
            Log.d("updateLocationView", "isRecording = ${locationTracker.isTracking}")
            textViewHomeLocation.text =
                "Your home location is defined as\n<${location.latitude}, " +
                        "${location.longitude}>"
            deleteHomeBtn.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrLocationView() {
        if (locationInfo != null && locationInfo?.latitude != null) {
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

    private fun setButtons() {
        deleteHomeBtn.visibility = View.INVISIBLE
        deletePhoneNumBtn.visibility = View.INVISIBLE
        testSmsBtn.visibility = View.INVISIBLE

        if (locationInfo != null) {
            fixSetHomeBtn.visibility = if (locationInfo?.accuracy!! <= 50) View.VISIBLE
            else View.INVISIBLE
        }

        trackingBtn.setOnClickListener {
            when {
                isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> {
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
                else -> ActivityCompat.requestPermissions(this,
                                                          arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                                          REQUEST_CODE_PERMISSION_GPS)
            }
        }

        setPhoneNumBtn.setOnClickListener {
            when {
                isPermissionGranted(Manifest.permission.SEND_SMS) -> {
                    setPhoneNumber()
                } else -> ActivityCompat.requestPermissions(this,
                                                            arrayOf(Manifest.permission.SEND_SMS),
                                                            REQUEST_CODE_PERMISSION_SMS)

            }
        }

        testSmsBtn.setOnClickListener {
            Log.d("testSms", "phone number = ${appContext.appSP.getPhoneNumber()}")
            val intent = Intent("POST_PC.ACTION_SEND_SMS")
            intent.putExtra("PHONE", appContext.appSP.getPhoneNumber())
            intent.putExtra("CONTENT",  "Honey I'm Sending a Test Message!")
            this.sendBroadcast(intent)
//            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        deletePhoneNumBtn.setOnClickListener {
            appContext.appSP.deletePhoneNumber()
            testSmsBtn.visibility = View.INVISIBLE
            deletePhoneNumBtn.visibility = View.INVISIBLE
            Toast.makeText(applicationContext, "Deleted phone number", Toast.LENGTH_SHORT)
                .show()
        }

        fixSetHomeBtn.setOnClickListener {
            if (locationInfo != null) {
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

    private fun setPhoneNumber() {
        // set the EditText
        val input = EditText(this@MainActivity)
        input.inputType = InputType.TYPE_CLASS_PHONE
        input.setRawInputType(Configuration.KEYBOARD_12KEY)     // TODO - what's do

        val builder = AlertDialog.Builder(this@MainActivity)

        with(builder)
        {
            setTitle("Insert Phone Number")
            setMessage("Please insert the phone number")
            setView(input)
            setPositiveButton("ok") { _: DialogInterface, _: Int ->
                if (input.text.toString().trim().isEmpty()) {
                    Toast.makeText(
                        applicationContext
                        , "Can't insert empty number"
                        , Toast.LENGTH_LONG
                    )
                        .show()
                    setPhoneNumber()
                } else {
                    appContext.appSP.storePhoneNumber(input.text.toString())
                    testSmsBtn.visibility = View.VISIBLE
                    deletePhoneNumBtn.visibility = View.VISIBLE
                }
            }
            setNegativeButton("cancel") { _: DialogInterface, _: Int -> }
            show()
        }
    }

    private fun buildAlertMessageNoGps(){
        val builder = android.app.AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->   // TODO
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(intent)   // TODO
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
            }
        val alert: android.app.AlertDialog = builder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_GPS -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(LOG_PERMISSION, "Asked more than once")
                        // reached here? means we asked the user for this permission more than once,
                        // and they still refuse. This would be a good time to open up a dialog
                        // explaining why we need this permission
                        Toast.makeText(
                            applicationContext
                            , "We need your GPS permission in order to start tracking"
                            , Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    Log.d(LOG_PERMISSION, "Permission has been denied by user")
                } else {
                    trackingBtn.text = TEXT_STOP_TRACKING
                    locationTracker.startTracking()
                    Log.d(LOG_PERMISSION, "Permission has been granted by user")
                }
            } REQUEST_CODE_PERMISSION_SMS -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.SEND_SMS)) {
                        Log.d(LOG_PERMISSION, "Asked for send SMS more than once")
                        // reached here? means we asked the user for this permission more than once,
                        // and they still refuse. This would be a good time to open up a dialog
                        // explaining why we need this permission
                        Toast.makeText(applicationContext,
                            "We need your SMS permission in order to operate",
                            Toast.LENGTH_LONG)
                            .show()
                    }
                    Log.d(LOG_PERMISSION, "Permission SMS has been denied by user")
                } else {
                    setPhoneNumber()
                }
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopTracking()
        unregisterReceiver(broadcastLocationReceiver)
        unregisterReceiver(broadcastStartTrackingReceiver)
        unregisterReceiver(broadcastStopTrackingReceiver)
        unregisterReceiver(broadcastGpsOffReceiver)
//        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(broadcastLocationReceiver)
//        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(broadcastStartTrackingReceiver)
//        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(broadcastStopTrackingReceiver)
//        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(broadcastGpsOffReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TRACK_TEXT, trackingBtn.text as String?)
        outState.putBoolean(KEY_IS_TRACKING, locationTracker.isTracking)
        val locationObjectAsJson = gson.toJson(locationInfo)
        outState.putString(KEY_LOCATION_INFO_OBJECT, locationObjectAsJson)
    }
}
