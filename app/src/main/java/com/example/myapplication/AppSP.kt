package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import android.util.Log
import com.google.gson.reflect.TypeToken

class AppSP(private val context: Context) {
    private val gson: Gson = Gson()
    private val spForHomeLoc: SharedPreferences =
        context.getSharedPreferences(SP_LOCATION_FILE_NAME, Context.MODE_PRIVATE)
    private var homeLocationInfo: LocationInfo? = LocationInfo()

    companion object {
        private const val SP_LOCATION_FILE_NAME: String = "sp_location"
        private const val GSON_KEY_LOCATION: String = "sp_gson_location"
        private const val NULL_TAG: String = "init_sp"
    }

    private fun loadHomeLocation(){
        val locAsJason: String? = spForHomeLoc.getString(GSON_KEY_LOCATION, null)
        if (locAsJason != null) {
            val locationType = object : TypeToken<LocationInfo>(){}.type
            homeLocationInfo = gson.fromJson(locAsJason, locationType)
        }
    }

    fun getHomeLocation(): LocationInfo? {
        return homeLocationInfo
    }

    fun setHomeLocation(homeLocationInfo: LocationInfo) {
        this.homeLocationInfo = homeLocationInfo
    }

    fun storeHomeLocation(){
        val locationAsJso = gson.toJson(homeLocationInfo)
        val edit: SharedPreferences.Editor = spForHomeLoc.edit()
        edit.putString(GSON_KEY_LOCATION, locationAsJso).
                apply()
    }

    fun deleteHomeLocation() {
        Log.d(NULL_TAG, "home loc = $homeLocationInfo")
        val edit: SharedPreferences.Editor = spForHomeLoc.edit()
        edit.remove(GSON_KEY_LOCATION).
                apply()
        homeLocationInfo = null
    }
}