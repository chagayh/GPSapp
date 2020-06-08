package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import android.util.Log

class AppDB(private val context: Context) {
    private val gson: Gson = Gson()
    private val spForHomeLoc: SharedPreferences

    companion object {
        private const val SP_TODO_LIST_FILE_NAME: String = "sp_todo_list"
        private const val KEY_TODO_LIST: String = "key_todo_list"
        private const val SIZE_TAG: String = "List Size"
    }

    init {
        spForHomeLoc = context.getSharedPreferences(SP_TODO_LIST_FILE_NAME, Context.MODE_PRIVATE)
        Log.d(SIZE_TAG, "Init AppDB")
    }

    private fun loadHomeLocation(){
        val locAsJason: String? = spForHomeLoc.getString(KEY_TODO_LIST, null)
        if (locAsJason != null) {
            val listType = object : TypeToken<ArrayList<Item>>(){}.type
            itemsList.addAll(gson.fromJson(locAsJason, listType))    // TODO check if correct
        }
    }

    fun storeHomeLocation(){
        val listAsJson: String = gson.toJson(itemsList)
        val edit: SharedPreferences.Editor = spForHomeLoc.edit()
        edit.putString(KEY_TODO_LIST, listAsJson).
        apply()
    }

    fun deleteHomeLocation() {

    }

}