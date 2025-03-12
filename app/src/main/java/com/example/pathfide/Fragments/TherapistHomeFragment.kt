package com.example.pathfide.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.example.pathfide.databinding.FragmentHomeTherapistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TherapistHomeFragment : Fragment() {

    private var _binding: FragmentHomeTherapistBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val TAG = "TherapistHomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeTherapistBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        fetchData()
    }

    private fun fetchData() {
        if (!isAdded) return
        fetchAppointmentRequests()
        fetchUpcomingSessions()
        fetchClinicalHours()
    }

    private fun fetchAppointmentRequests() {
        val therapistId = auth.currentUser?.uid ?: return

        db.collection("scheduledSessions")
            .whereEqualTo("therapistId", therapistId)
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                try {
                    val requestCount = documents.size()
                    _binding?.apply {
                        appointmentValue.text = requestCount.toString()
                        appointmentDetails.text = "You have $requestCount appointment requests"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating appointment UI", e)
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e(TAG, "Error fetching appointment requests: ${e.message}")
                showToast("Error fetching appointment requests")
            }
    }

    private fun fetchUpcomingSessions() {
        val therapistId = auth.currentUser?.uid ?: return

        db.collection("appointmentResponse")
            .whereEqualTo("therapistId", therapistId)
            .whereEqualTo("isAccepted", true)
            .get()
            .addOnSuccessListener { responseDocuments ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val acceptedAppointmentIds = responseDocuments.documents.mapNotNull {
                    it.getString("appointmentId")
                }

                if (acceptedAppointmentIds.isEmpty()) {
                    updateSessionUI(0)
                    return@addOnSuccessListener
                }

                db.collection("scheduledSessions")
                    .whereIn("appointmentId", acceptedAppointmentIds)
                    .get()
                    .addOnSuccessListener { sessionDocuments ->
                        if (!isAdded || _binding == null) return@addOnSuccessListener
                        updateSessionUI(sessionDocuments.size())
                    }
                    .addOnFailureListener { e ->
                        if (!isAdded) return@addOnFailureListener
                        Log.e(TAG, "Error fetching sessions: ${e.message}")
                        showToast("Error fetching sessions")
                    }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e(TAG, "Error fetching appointment responses: ${e.message}")
                showToast("Error fetching appointments")
            }
    }

    private fun updateSessionUI(sessionCount: Int) {
        _binding?.apply {
            sessionValue.text = sessionCount.toString()
            sessionDetails.text = "You have $sessionCount upcoming sessions"
        }
    }

    private fun fetchClinicalHours() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                try {
                    _binding?.clinicalHoursTextView?.setText(document?.getString("clinicalHours") ?: "")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating clinical hours UI", e)
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                showToast("Error fetching clinical hours: ${e.message}")
            }
    }

    private fun setupListeners() {
        _binding?.chatPageIcon?.setOnClickListener {
            try {
                navigateToFragment(R.id.action_TherapistHomeFragment_to_chatMainFragment)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to navigate to Chat Fragment: ${e.message}")
            }
        }
    }

    private fun navigateToFragment(actionId: Int) {
        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
