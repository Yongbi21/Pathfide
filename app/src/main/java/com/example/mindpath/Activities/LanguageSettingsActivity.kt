package com.example.mindpath.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.mindpath.R
import com.example.mindpath.utils.QuestionManager
import com.google.android.material.snackbar.Snackbar

class LanguageSettingsActivity : AppCompatActivity() {
    private var languageSelected: String? = null
    private lateinit var confirmButton: Button
    private lateinit var languageSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        // Initialize views
        languageSpinner = findViewById(R.id.languageSpinner)
        confirmButton = findViewById(R.id.confirmButton)

        // Get current language
        val currentLanguage = LocaleHelper.getCurrentLocale(this)
        Log.d("LanguageSettings", "Current Language onCreate: $currentLanguage")

        // Set initial spinner position
        languageSpinner.setSelection(getSpinnerPosition(currentLanguage))

        setupSpinner()
        setupButtons()
    }

    private fun setupSpinner() {
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newLanguage = when (position) {
                    0 -> "en"  // English
                    1 -> "tl"  // Tagalog
                    2 -> "ceb" // Bisaya
                    else -> "en"
                }

                if (languageSelected != newLanguage) {
                    languageSelected = newLanguage
                    Log.d("LanguageSettings", "Selected Language: $languageSelected")

                    val currentLanguage = LocaleHelper.getCurrentLocale(this@LanguageSettingsActivity)
                    if (languageSelected != currentLanguage) {
                        // Update shared preferences
                        getSharedPreferences("AppSettings", MODE_PRIVATE).edit()
                            .putString("Language", languageSelected)
                            .apply()

                        // Update locale
                        LocaleHelper.setLocale(this@LanguageSettingsActivity, languageSelected!!)
                        Log.d("LanguageSettings", "Locale set to: $languageSelected")

                        // Update QuestionManager
                        when (languageSelected) {
                            "en" -> QuestionManager.changeLanguage("english")
                            "tl" -> QuestionManager.changeLanguage("tagalog")
                            "ceb" -> QuestionManager.changeLanguage("bisaya")
                        }

                        // Update UI
                        updateUITexts()

                        // Enable confirm button
                        confirmButton.isEnabled = true
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupButtons() {
        // Setup confirm button
        confirmButton.setOnClickListener {
            if (languageSelected != null) {
                // Disable the button to prevent multiple clicks
                confirmButton.isEnabled = false

                // Show processing message
                Snackbar.make(it, R.string.language_change_processing, Snackbar.LENGTH_SHORT).show()

                // Add a slight delay to show the processing state
                Handler(Looper.getMainLooper()).postDelayed({
                    // Recreate the activity to apply the new locale

                    // Show confirmation message after recreation
                    showConfirmationSnackbar(languageSelected!!)

                    // Re-enable the button
                    confirmButton.isEnabled = true
                }, 1000)
            }
        }

        // Setup back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        // Get saved language
        val savedLanguage = LocaleHelper.getCurrentLocale(this)
        Log.d("LanguageSettings", "Saved Language onResume: $savedLanguage")

        // Update spinner position
        languageSpinner.setSelection(getSpinnerPosition(savedLanguage))

        // Update UI texts
        updateUITexts()
    }

    private fun updateUITexts() {
        // Update all text elements in the UI
        confirmButton.setText(R.string.confirm_button)
        findViewById<ImageView>(R.id.backButton).contentDescription = getString(R.string.back_button)

        // Additional UI updates can be added here
    }

    private fun getSpinnerPosition(languageCode: String): Int {
        return when (languageCode) {
            "en" -> 0
            "tl" -> 1
            "ceb" -> 2
            else -> 0
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun showConfirmationSnackbar(language: String) {
        val message = getString(R.string.language_changed_message, language)
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.getLocalizedContext(newBase))
    }
}