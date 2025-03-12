package com.example.pathfide.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pathfide.R
import java.text.SimpleDateFormat
import java.util.*

class MoodTrackerFragment : Fragment() {

    private lateinit var moodRadioGroup: RadioGroup
    private lateinit var moodDescription: EditText
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moodRadioGroup = view.findViewById(R.id.moodRadioGroup)
        moodDescription = view.findViewById(R.id.moodDescription)
        firestore = FirebaseFirestore.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE)

        // Check if mood tracker was already completed today
        if (isMoodTrackerCompletedToday()) {
            navigateToMoodSummary()
        } else {
            setCurrentDate()
        }

        val submitButton = view.findViewById<Button>(R.id.moodButton)
        submitButton.setOnClickListener {
            saveMoodToLocal()
        }
    }

    private fun setCurrentDate() {
        val currentDate = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault()).format(Date())
        view?.findViewById<TextView>(R.id.date)?.text = "Today, $currentDate"
    }

    private fun isMoodTrackerCompletedToday(): Boolean {
        val lastCompletedDate = sharedPreferences.getString("lastMoodTrackerDate", null)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return lastCompletedDate == today
    }

    private fun saveMoodToLocal() {
        val selectedMoodId = moodRadioGroup.checkedRadioButtonId

        if (selectedMoodId == -1) {
            Toast.makeText(context, "Please select a mood", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMoodRadioButton = view?.findViewById<RadioButton>(selectedMoodId)
        val selectedMood = selectedMoodRadioButton?.text.toString()
        val description = moodDescription.text.toString().trim()

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Save completion date locally
        sharedPreferences.edit().putString("lastMoodTrackerDate", currentDate).apply()

        // Save locally for sync
        val moodData = "$currentUserId,$currentDate,$selectedMood,$description"
        sharedPreferences.edit().putString("lastMood", moodData).apply()

        // Save to Firestore
        saveMoodToFirestore(currentUserId, currentDate, selectedMood, description)
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
                Log.d("MoodTrackerFragment", "Mood data saved successfully to Firestore")
                Toast.makeText(context, "Mood saved successfully!", Toast.LENGTH_SHORT).show()
                navigateToMoodSummary()
            }
            .addOnFailureListener { exception ->
                Log.e("MoodTrackerFragment", "Error saving mood data to Firestore", exception)
                Toast.makeText(context, "Failed to save mood data to Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMoodSummary() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.moodTrackerFragment) {
            // Navigate to MoodSummaryFragment
            navController.navigate(R.id.action_moodTrackerFragment_to_moodSummaryFragment)
        } else {
            Log.e("MoodTrackerFragment", "Invalid navigation attempt: Current destination is not MoodTrackerFragment")
        }
    }
}
