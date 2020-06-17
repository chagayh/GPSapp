package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import android.util.Log
import com.google.gson.reflect.TypeToken

class AppSP(context: Context) {
    private val gson: Gson
    private val appContext: Context = context
    private val spForGPSapp: SharedPreferences
    private var homeLocationInfo: LocationInfo? = null
    private var phoneNumber: String? = null

    companion object {
        private const val SP_LOCATION_FILE_NAME: String = "sp_location"
        private const val GSON_KEY_LOCATION: String = "sp_gson_location"
        private const val KEY_PHONE_NUMBER: String = "phone_number"
        private const val NULL_TAG: String = "init_sp"
    }

    init {
        spForGPSapp = appContext.getSharedPreferences(SP_LOCATION_FILE_NAME, Context.MODE_PRIVATE)
        gson = Gson()
        loadHomeLocation()
        loadPhoneNumber()
    }

    private fun loadHomeLocation(){
        val locAsJason: String? = spForGPSapp.getString(GSON_KEY_LOCATION, null)
        if (locAsJason != null) {
            val locationType = object : TypeToken<LocationInfo>(){}.type
            homeLocationInfo = gson.fromJson(locAsJason, locationType)
        }
    }

    private fun loadPhoneNumber(){
        val phoneNum: String? = spForGPSapp.getString(KEY_PHONE_NUMBER, null)
        this.phoneNumber = phoneNum
    }

    fun storePhoneNumber(phoneNum: String){
        this.phoneNumber = phoneNum
        val edit: SharedPreferences.Editor = spForGPSapp.edit()
        edit.putString(KEY_PHONE_NUMBER, this.phoneNumber).
        apply()
    }

    fun deletePhoneNumber() {
        val edit: SharedPreferences.Editor = spForGPSapp.edit()
        edit.remove(KEY_PHONE_NUMBER).
        apply()
        phoneNumber = null
    }

    fun getHomeLocation(): LocationInfo? {
        return homeLocationInfo
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    fun storeHomeLocation(homeLocationInfo: LocationInfo?){
        this.homeLocationInfo = homeLocationInfo
        val locationAsJso = gson.toJson(homeLocationInfo)
        val edit: SharedPreferences.Editor = spForGPSapp.edit()
        edit.putString(GSON_KEY_LOCATION, locationAsJso).
                apply()
    }

    fun deleteHomeLocation() {
        Log.d(NULL_TAG, "home loc = $homeLocationInfo")
        val edit: SharedPreferences.Editor = spForGPSapp.edit()
        edit.remove(GSON_KEY_LOCATION).
                apply()
        homeLocationInfo = null
    }
}