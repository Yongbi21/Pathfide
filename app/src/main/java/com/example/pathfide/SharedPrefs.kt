package com.example.chatmessenger

import android.content.Context
import android.content.SharedPreferences

// Class for handling shared preferences
class SharedPrefs(context: Context) {

    // SharedPreferences instance
    private val prefs: SharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    // Function to set a String value in shared preferences
    fun setValue(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    // Function to get a String value from shared preferences
    fun getValue(key: String): String? {
        return prefs.getString(key, null)
    }

    // Function to set an integer value in shared preferences
    fun setIntValue(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    // Function to get an integer value from shared preferences
    fun getIntValue(key: String, defaultValue: Int): Int {
        // Changed parameter name i to defaultValue for clarity
        return prefs.getInt(key, defaultValue)
    }
}
