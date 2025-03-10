package com.example.mindpath

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.mindpath.R
import com.example.mindpath.Activities.WelcomeActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Simulate some loading time
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the welcome activity
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 2000) // 2 seconds delay
    }
}