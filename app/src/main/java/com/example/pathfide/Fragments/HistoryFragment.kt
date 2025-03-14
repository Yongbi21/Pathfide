package com.example.pathfide.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Adapter.AppointmentHistoryAdapter
import com.example.pathfide.Model.ScheduledSession
import com.example.pathfide.R
import com.example.pathfide.ViewModel.NotificationViewModel
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment() {
    private lateinit var userType: String
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var historyAdapter: AppointmentHistoryAdapter
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvHistoryTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationViewModel = ViewModelProvider(requireActivity())[NotificationViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initializeViews(view)
        userType = arguments?.getString("USER_TYPE") ?: "CLIENT"
        Log.d("HistoryFragment", "Determined userType: $userType")



        setupRecyclerView()
        notificationViewModel.fetchSessionsWithStatus(userType)
        setupObservers()




    }

    private fun initializeViews(view: View) {
        rvHistory = view.findViewById(R.id.rvHistory)
        tvHistoryTitle = view.findViewById(R.id.tvHistoryTitle)
    }

    private fun setupRecyclerView() {
        historyAdapter = AppointmentHistoryAdapter(userType)
        rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun setupObservers() {
        notificationViewModel.sessionsWithStatus.observe(viewLifecycleOwner) { sessions ->
            Log.d("HistoryFragment", "Observed sessions with status: $sessions")
            if (sessions != null && sessions.isNotEmpty()) {
                // Check for payment status directly in the fragment
                checkPaymentStatusForSessions(sessions)
            } else {
                Log.d("HistoryFragment", "No sessions to display, clearing adapter list.")
                historyAdapter.submitList(emptyList()) // Clear the adapter if no sessions
            }
        }
    }

    private fun checkPaymentStatusForSessions(sessions: List<ScheduledSession>) {
        val firestore = FirebaseFirestore.getInstance()
        val updatedSessions = sessions.toMutableList()
        var completedChecks = 0

        for (i in sessions.indices) {
            val session = sessions[i]

            // Use the session.id as the sessionId field in the query
            firestore.collection("payments")
                .whereEqualTo("appointmentId", session.appointmentId) // Use the same field name used in SessionStatusFragment
                .get()
                .addOnSuccessListener { paymentDocs ->
                    Log.d("HistoryFragment", "Found ${paymentDocs.size()} payment documents for session: ${session.id}")

                    if (!paymentDocs.isEmpty) {
                        // Found a payment for this session
                        val doc = paymentDocs.documents[0] // Take the first matching payment doc
                        val paymentStatus = doc.getString("status") ?: ""

                        Log.d("HistoryFragment", "Payment doc: id=${doc.id}, status='$paymentStatus'")

                        val newStatus = when(paymentStatus.trim()) {
                            "Payment Successful!" -> "Paid"
                            "Paid" -> "Paid"
                            "Payment Declined" -> "Declined"
                            "Declined" -> "Declined"
                            else -> "Pending"
                        }

                        updatedSessions[i] = session.copy(paymentStatus = newStatus)
                    } else {
                        // No payment found for this session
                        updatedSessions[i] = session.copy(paymentStatus = "Pending")
                        Log.d("HistoryFragment", "No payment found for session: ${session.id}")
                    }

                    completedChecks++
                    if (completedChecks == sessions.size) {
                        historyAdapter.submitList(updatedSessions)
                        Log.d("HistoryFragment", "All payment checks completed, updated adapter")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HistoryFragment", "Error checking payment status", e)
                    updatedSessions[i] = session.copy(paymentStatus = "Pending")
                    completedChecks++
                    if (completedChecks == sessions.size) {
                        historyAdapter.submitList(updatedSessions)
                    }
                }
        }

        // Safety measure
        Handler(Looper.getMainLooper()).postDelayed({
            if (completedChecks < sessions.size) {
                Log.d("HistoryFragment", "Safety timeout - updating adapter with partial results")
                historyAdapter.submitList(updatedSessions)
            }
        }, 3000)
    }}
