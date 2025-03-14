package com.example.pathfide.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Adapter.AppointmentHistoryAdapter
import com.example.pathfide.Model.ScheduledSession
import com.example.pathfide.R
import com.example.pathfide.ViewModel.NotificationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SessionStatusFragment : Fragment() {
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var sessionStatusAdapter: AppointmentHistoryAdapter
    private lateinit var rvSessionStatusRecyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "SessionStatusFragment"
    private var sessionListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationViewModel = ViewModelProvider(requireActivity())[NotificationViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_session_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)

        // Force admin user type
        val userType = "ADMIN"
        Log.d(TAG, "Setting userType: $userType")

        setupRecyclerView(userType)
        setupRealtimeSessionDoneListener()
    }

    private fun initializeViews(view: View) {
        rvSessionStatusRecyclerView = view.findViewById(R.id.rvSessionStatusRecyclerView)
    }

    private fun setupRecyclerView(userType: String) {
        sessionStatusAdapter = AppointmentHistoryAdapter(userType)
        rvSessionStatusRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sessionStatusAdapter
        }
    }

    private fun setupRealtimeSessionDoneListener() {
        // Setup real-time listener for sessions with "Session Done" status
        sessionListener = firestore.collection("scheduledSessions")
            .whereEqualTo("sessionStatus", "Session Done")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for completed sessions", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val sessions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ScheduledSession::class.java)?.copy(
                            appointmentId = doc.id,
                            id = doc.id
                        )
                    }

                    Log.d(TAG, "Real-time update: Fetched ${sessions.size} completed sessions")
                    processCompletedSessions(sessions)
                } else {
                    Log.d(TAG, "No completed sessions in real-time update")
                    sessionStatusAdapter.submitList(emptyList())
                }
            }
    }

    private fun processCompletedSessions(sessions: List<ScheduledSession>) {
        var processedCount = 0
        val updatedSessions = mutableListOf<ScheduledSession>()

        if (sessions.isEmpty()) {
            sessionStatusAdapter.submitList(emptyList())
            return
        }

        for (session in sessions) {
            // Check payment status
            firestore.collection("payments")
                .whereEqualTo("sessionId", session.id)
                .get()
                .addOnSuccessListener { paymentSnapshot ->
                    val paymentStatus = if (!paymentSnapshot.isEmpty &&
                        paymentSnapshot.documents.firstOrNull()?.getString("status") == "Payment Successful!") {
                        "Paid"
                    } else {
                        "Pending"
                    }

                    // Check acceptance status
                    firestore.collection("appointmentResponse")
                        .whereEqualTo("appointmentId", session.id)
                        .get()
                        .addOnSuccessListener { responseSnapshot ->
                            val isAccepted = if (!responseSnapshot.isEmpty) {
                                responseSnapshot.documents.firstOrNull()?.getBoolean("isAccepted")
                            } else {
                                true // Assume accepted for completed sessions
                            }

                            val updatedSession = session.copy(
                                paymentStatus = paymentStatus,
                                isAccepted = isAccepted
                            )

                            updatedSessions.add(updatedSession)
                            processedCount++

                            // When all sessions are processed, update the adapter
                            if (processedCount == sessions.size) {
                                // Sort sessions by date (newest first)
                                val sortedSessions = updatedSessions.sortedByDescending {
                                    it.appointmentDate + " " + it.appointmentTime
                                }

                                Log.d(TAG, "Processed all completed sessions: ${sortedSessions.size}")
                                sessionStatusAdapter.submitList(sortedSessions)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error checking acceptance status for session ${session.id}", e)
                            processedCount++

                            if (processedCount == sessions.size) {
                                sessionStatusAdapter.submitList(updatedSessions)
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking payment status for session ${session.id}", e)
                    processedCount++

                    if (processedCount == sessions.size) {
                        sessionStatusAdapter.submitList(updatedSessions)
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener to prevent memory leaks
        sessionListener?.remove()
    }
}