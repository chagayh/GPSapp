package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

class LocalSendSmsBroadcastReceiver(private val context: Context,
                                    private val appContext: GPSapp): BroadcastReceiver() {
    public val PHONE: String = "PHONE"
    public val CONTENT: String = "CONTENT"
    private val REQUEST_CODE_PERMISSION_SMS = 1112
    private var intent: Intent? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        this.intent = intent
        if (!checkPermission()) {
//            ActivityCompat.requestPermissions(
//                activity,
//                arrayOf(Manifest.permission.SEND_SMS),
//                REQUEST_CODE_PERMISSION_SMS)
            Log.d("permission", "Permission not granted")
            return
        } else {
            sendSms()
        }
    }

    private fun sendSms(){
        val phoneNumber = intent?.getStringExtra(PHONE)
        val smsContent = intent?.getStringExtra(CONTENT)
        if (phoneNumber == null || smsContent == null) {
            Log.e("phoneOrContent", "phone or content = null")
            return
        }
        SmsManager.getDefault().sendTextMessage(
            phoneNumber,
            null,
            smsContent,
            null,
            null)
        appContext.notificationFireHelper.fireNotification("sending sms to $phoneNumber: $smsContent")
    }

    private fun checkPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)
    }

//    fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>,
//                                            grantResults: IntArray) {
//        when (requestCode) {
//            REQUEST_CODE_PERMISSION_SMS -> {
//
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(
//                            activity,
//                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Log.d("permission", "Asked more than once")
//                        // reached here? means we asked the user for this permission more than once,
//                        // and they still refuse. This would be a good time to open up a dialog
//                        // explaining why we need this permission
//                        Toast.makeText(appContext
//                            ,"We need your GPS permission in order to start tracking"
//                            , Toast.LENGTH_LONG)
//                            .show()
//                    }
//                    Log.d("permission", "Permission has been denied by user")
//                } else {
//                    sendSms()
//                }
//            }
//        }
//    }
}