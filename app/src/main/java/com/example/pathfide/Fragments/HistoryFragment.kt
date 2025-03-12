package com.example.pathfide.fragments

import android.os.Bundle
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
import com.example.pathfide.R
import com.example.pathfide.ViewModel.NotificationViewModel

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
                historyAdapter.submitList(sessions)
            } else {
                Log.d("HistoryFragment", "No sessions to display, clearing adapter list.")
                historyAdapter.submitList(emptyList()) // Clear the adapter if no sessions
            }
        }
    }
}
