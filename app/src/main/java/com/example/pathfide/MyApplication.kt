package com.example.pathfide

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
        const val activityResultOk = android.app.Activity.RESULT_OK
        private const val PERMISSION_REQUEST_CODE = 123

    }

    private lateinit var db: FirebaseFirestore



    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        // Initialize global resources or libraries
        initializeAnalytics()
        setupDatabase()
        setupLanguagePreferences()
        setupDependencyInjection()
        FirebaseApp.initializeApp(this)

    }

    private fun initializeAnalytics() {
        // Initialize your analytics library
        Log.d("MyApplication", "Analytics initialized")
    }

    private fun setupDatabase() {
        // Initialize your database
        Log.d("MyApplication", "Database initialized")
    }

    private fun setupLanguagePreferences() {
        // Initialize language preferences
        Log.d("MyApplication", "Language preferences initialized")
    }

    private fun setupDependencyInjection() {
        // Initialize Hilt or Dagger
        Log.d("MyApplication", "Dependency injection initialized")
    }

    // Method to set the user's online status to offline
    fun forceOfflineStatus() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>(
            "isOnline" to false, // Set user status to offline
            "lastSeen" to System.currentTimeMillis()  // Update last seen time
        )

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("MyApplication", "User status set to offline successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("MyApplication", "Error setting user status to offline", e)
            }
    }

    fun getAppContext(): Context {
        return applicationContext
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Handle configuration changes, such as language changes
        Log.d("MyApplication", "Configuration changed")
    }


}
