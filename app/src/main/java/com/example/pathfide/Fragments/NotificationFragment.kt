package com.example.pathfide.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Import the correct delegate
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Adapter.ClientScheduledSessionsAdapter
import com.example.pathfide.Adapter.PaymentNotificationAdapter
import com.example.pathfide.Adapter.TherapistScheduledSessionsAdapter
import com.example.pathfide.Model.ScheduledSession
import com.example.pathfide.R
import com.example.pathfide.ViewModel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth

class NotificationFragment : Fragment() {

    private lateinit var userType: String
    private val viewModel: NotificationViewModel by viewModels() // Use by viewModels()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var paymentNotificationAdapter: PaymentNotificationAdapter
    private lateinit var sessionNotificationRecyclerView: RecyclerView
    private lateinit var paymentNotificationRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userType = arguments?.getString("USER_TYPE") ?: "CLIENT"
        Log.d("NotificationFragment", "UserType set to: $userType")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionNotificationRecyclerView = view.findViewById(R.id.sessionNotificationRecyclerView)
        paymentNotificationRecyclerView = view.findViewById(R.id.paymentNotificationRecyclerView)

        // Setup payment notifications
        paymentNotificationRecyclerView.layoutManager = LinearLayoutManager(context)
        paymentNotificationAdapter = PaymentNotificationAdapter(emptyList(), userType)
        paymentNotificationRecyclerView.adapter = paymentNotificationAdapter

        // Setup session notifications based on user type
        sessionNotificationRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe payment notifications
        viewModel.paymentNotifications.observe(viewLifecycleOwner) { notifications ->
            val sortedNotifications = notifications.sortedByDescending { it.timestamp.toDate().time }
            Log.d("NotificationFragment", "Sorted Payment Notifications: $sortedNotifications")
            paymentNotificationAdapter.updateNotifications(sortedNotifications)
            paymentNotificationRecyclerView.adapter?.notifyDataSetChanged()
        }

        // Set user type
        viewModel.setUserType(userType)

        // Fetch sessions with acceptance status
        viewModel.fetchSessionsWithStatus(userType)
        viewModel.observePaymentNotifications()


        when (userType) {
            "CLIENT" -> {
                viewModel.sessionsWithStatus.observe(viewLifecycleOwner) { sessions ->
                    val sortedSessions = sessions.sortedByDescending {
                        it.responseTimestamp.takeIf { it > 0 } ?: it.timestamp.toDate().time
                    }
                    Log.d("NotificationFragment", "Sorted Session Notifications: $sortedSessions")
                    setupClientNotifications(sortedSessions)
                }
            }
            "THERAPIST" -> {
                viewModel.sessionsWithStatus.observe(viewLifecycleOwner) { sessions ->

                    val sortedSessions = sessions.sortedByDescending {
                        it.responseTimestamp.takeIf { it > 0 } ?: it.timestamp.toDate().time
                    }
                    Log.d("NotificationFragment", "Sorted Therapist Notifications: $sortedSessions")
                    setupTherapistNotifications(sortedSessions)
                }
            }
        }

        // Start observing notifications with correct userType
        viewModel.observeNotifications(userType)

    }


    private fun setupClientNotifications(sessions: List<ScheduledSession> = emptyList()) {
        val sortedSessions = sessions.sortedByDescending { it.timestamp }
        val adapter = ClientScheduledSessionsAdapter(sortedSessions) { session ->
            navigateToPaymentDetails(session)
        }
        sessionNotificationRecyclerView.adapter = adapter
    }

    private fun setupTherapistNotifications(sessions: List<ScheduledSession> = emptyList()) {
        val sortedSessions = sessions.sortedByDescending { it.timestamp }
        val adapter = TherapistScheduledSessionsAdapter(sortedSessions) { session ->
            navigateToSessionDetails(session)
        }
        sessionNotificationRecyclerView.adapter = adapter
    }


    private fun navigateToPaymentDetails(session: ScheduledSession) {
        // Create a bundle with necessary details if needed
        val bundle = Bundle().apply {
            putString("appointmentDate", session.appointmentDate)
            putString("appointmentTime", session.appointmentTime)
            putString("sessionType", session.sessionType)
            putString("firstName", session.firstName)
            putString("lastName", session.lastName)
            putString("appointmentId", session.id)
            putString("therapistId", session.therapistId)
        }

        // Navigate to the payment fragment
        findNavController().navigate(
            R.id.action_notificationFragment_to_paymentFragment,
            bundle
        )
    }

    private fun navigateToSessionDetails(session: ScheduledSession) {
        // Create bundle with session details
        val bundle = Bundle().apply {
            putString("appointmentDate", session.appointmentDate)
            putString("appointmentTime", session.appointmentTime)
            putString("sessionType", session.sessionType)
            putString("firstName", session.firstName)
            putString("lastName", session.lastName)
            putString("appointmentId", session.id)

        }

        // Navigate to your details fragment
        findNavController().navigate(
            R.id.action_notificationFragment_to_appointmentRequestFragment,
            bundle
        )
    }
    override fun onResume() {
        super.onResume()
        viewModel.markAllNotificationsAsRead()
        Log.d("NotificationFragment", "Fragment resumed, marking notifications as read.")
    }
    override fun onStop() {
        super.onStop()
        Log.d("NotificationFragment", "Fragment stopped.")
    }
}
