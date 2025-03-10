package com.example.pathfide.fragments

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MoodSummaryFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressCircle: ProgressBar
    private lateinit var progressPercentage: TextView
    private lateinit var happyPercentageTextView: TextView
    private lateinit var sickPercentageTextView: TextView
    private lateinit var madPercentageTextView: TextView
    private lateinit var anxiousPercentageTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var selfCareSuggestionTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE)

        // Initialize views
        progressCircle = view.findViewById(R.id.progressCircle)
        progressPercentage = view.findViewById(R.id.progressPercentage)
        happyPercentageTextView = view.findViewById(R.id.happyPercentageTextView)
        sickPercentageTextView = view.findViewById(R.id.sickPercentageTextView)
        madPercentageTextView = view.findViewById(R.id.madPercentageTextView)
        anxiousPercentageTextView = view.findViewById(R.id.anxiousPercentageTextView)
        dateTextView = view.findViewById(R.id.dateTextView)
        selfCareSuggestionTextView = view.findViewById(R.id.selfCareSuggestionTextView)

        // Set current date
        setCurrentDate()

        // Sync local data with Firestore if there is internet
        if (isInternetAvailable()) {
            syncLocalMoodData()
        } else {
            // If no internet, just fetch mood data from Firestore
            fetchMoodData()
        }

        val homeButton = view.findViewById<Button>(R.id.homeButton)
        homeButton.setOnClickListener {
            findNavController().navigate(R.id.action_moodSummaryFragment_to_homeFragment)
        }
    }

    private fun setCurrentDate() {
        val currentDate = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault()).format(Date())
        dateTextView.text = "Today, $currentDate"
    }

    private fun fetchMoodData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current month in the "YYYY-MM" format
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        Log.d("MoodSummaryFragment", "Fetching mood data for the month: $currentMonth for user: $currentUserId")

        firestore.collection("moodTracker")
            .whereEqualTo("userId", currentUserId) // Filter by user ID
            .whereGreaterThanOrEqualTo("date", currentMonth) // Filter by current month
            .get()
            .addOnSuccessListener { documents ->
                Log.d("MoodSummaryFragment", "Fetched ${documents.size()} documents for user: $currentUserId")
                val moodCounts = mutableMapOf(
                    "Happy" to 0,
                    "Anxious" to 0,
                    "Mad" to 0,
                    "Sick" to 0
                )

                for (document in documents) {
                    val mood = document.getString("mood")
                    val date = document.getString("date")
                    Log.d("MoodSummaryFragment", "Document: mood=$mood, date=$date")
                    if (mood != null) {
                        moodCounts[mood] = moodCounts.getOrDefault(mood, 0) + 1
                    }
                }

                Log.d("MoodSummaryFragment", "Mood counts: $moodCounts")
                updateUI(moodCounts, documents.size())
            }
            .addOnFailureListener { exception ->
                Log.e("MoodSummaryFragment", "Error fetching mood data", exception)
                Toast.makeText(context, "Failed to load mood data", Toast.LENGTH_SHORT).show()
                updateUI(emptyMap(), 0)
            }
    }

    private fun syncLocalMoodData() {
        val moodData = sharedPreferences.getString("lastMood", null)
        if (moodData != null) {
            val parts = moodData.split(",")
            if (parts.size == 4) {
                val userId = parts[0]
                val date = parts[1]
                val mood = parts[2]
                val description = parts[3]
                saveMoodToFirestore(userId, date, mood, description)
                // Clear local data after syncing
                sharedPreferences.edit().remove("lastMood").apply()
            }
        } else {
            fetchMoodData() // No local data to sync, fetch from Firestore instead
        }
    }

    private fun saveMoodToFirestore(userId: String, date: String, mood: String, description: String) {
        val moodData = hashMapOf(
            "userId" to userId,
            "date" to date,
            "mood" to mood,
            "description" to description
        )

        firestore.collection("moodTracker")
            .add(moodData)
            .addOnSuccessListener {
                Log.d("MoodSummaryFragment", "Mood data saved successfully to Firestore")
                Toast.makeText(context, "Mood synced successfully!", Toast.LENGTH_SHORT).show()
                fetchMoodData() // Fetch updated mood data after syncing
            }
            .addOnFailureListener { exception ->
                Log.e("MoodSummaryFragment", "Error saving mood data to Firestore", exception)
                Toast.makeText(context, "Failed to sync mood data to Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(moodCounts: Map<String, Int>, totalEntries: Int) {
        Log.d("MoodSummaryFragment", "Updating UI with total entries: $totalEntries")
        if (totalEntries == 0) {
            progressCircle.progress = 0
            progressPercentage.text = "0%"
            happyPercentageTextView.text = "0% Happy"
            anxiousPercentageTextView.text = "0% Anxious"
            madPercentageTextView.text = "0% Mad"
            sickPercentageTextView.text = "0% Sick"
            updateSelfCareSuggestion(0)
            return
        }

        val happyPercentage = calculatePercentage(moodCounts["Happy"] ?: 0, totalEntries)
        val anxiousPercentage = calculatePercentage(moodCounts["Anxious"] ?: 0, totalEntries)
        val madPercentage = calculatePercentage(moodCounts["Mad"] ?: 0, totalEntries)
        val sickPercentage = calculatePercentage(moodCounts["Sick"] ?: 0, totalEntries)

        progressCircle.progress = happyPercentage
        progressPercentage.text = "$happyPercentage%"

        happyPercentageTextView.text = "$happyPercentage% Happy"
        anxiousPercentageTextView.text = "$anxiousPercentage% Anxious"
        madPercentageTextView.text = "$madPercentage% Mad"
        sickPercentageTextView.text = "$sickPercentage% Sick"

        updateSelfCareSuggestion(happyPercentage)
    }

    private fun calculatePercentage(count: Int, total: Int): Int {
        return if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    }

    private fun updateSelfCareSuggestion(happyPercentage: Int) {
        val suggestion = when {
            happyPercentage >= 80 -> "Great job maintaining a positive mood! Keep up your current self-care routines."
            happyPercentage >= 60 -> "You're feeling great this month! Keep up the good work by continuing to engage in activities that bring you joy and fulfillment."
            happyPercentage >= 40 -> "This month, feelings of anxiety or frustration seem to have been dominant. Try practicing mindfulness or calming activities."
            happyPercentage >= 20 -> "It looks like this month has been challenging. Now might be a great time to explore self-care strategies that can help ease stress or emotional strain."
            else -> "This month has been challenging. It's important to prioritize self-care and seek support if needed."
        }
        selfCareSuggestionTextView.text = suggestion
        Log.d("MoodSummaryFragment", "Updated self-care suggestion: $suggestion")
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
