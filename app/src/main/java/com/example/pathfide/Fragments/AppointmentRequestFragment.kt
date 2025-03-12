package com.example.pathfide.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.example.pathfide.ViewModel.NotificationViewModel
import com.google.android.material.snackbar.Snackbar

class AppointmentRequestFragment : Fragment() {
    private lateinit var viewModel: NotificationViewModel

    // View references
    private lateinit var tvClientName: TextView
    private lateinit var tvSessionType: TextView
    private lateinit var tvSessionDate: TextView
    private lateinit var tvSessionTime: TextView
    private lateinit var tvStatus: TextView
    private lateinit var acceptBtn: Button
    private lateinit var rejectBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(NotificationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_appointment_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)

        // Set appointment details from arguments
        setAppointmentDetails()

        // Initially show the buttons until we verify the appointment status
        showButtons()

        // Check the appointment status to manage button visibility
        checkAppointmentStatus()

        // Set up button click listeners
        setupClickListeners()

        // Observe response status
        observeResponseStatus()
    }

    private fun initializeViews(view: View) {
        tvClientName = view.findViewById(R.id.tvClientName)
        tvSessionType = view.findViewById(R.id.tvSessionType)
        tvSessionDate = view.findViewById(R.id.tvSessionDate)
        tvSessionTime = view.findViewById(R.id.tvSessionTime)
        tvStatus = view.findViewById(R.id.tvStatus)
        acceptBtn = view.findViewById(R.id.acceptBtn)
        rejectBtn = view.findViewById(R.id.rejectBtn)
    }

    private fun setAppointmentDetails() {
        arguments?.let { args ->
            val firstName = args.getString("firstName", "")
            val lastName = args.getString("lastName", "")
            val clientName = "$firstName $lastName"

            tvClientName.text = clientName
            tvSessionType.text = args.getString("sessionType", "")
            tvSessionDate.text = args.getString("appointmentDate", "")
            tvSessionTime.text = args.getString("appointmentTime", "")
            tvStatus.text = "For Confirmation"
        }
    }

    private fun checkAppointmentStatus() {
        arguments?.getString("appointmentId")?.let { appointmentId ->
            if (appointmentId.isNotBlank()) {
                Log.d("AppointmentRequestFragment", "Checking status for appointmentId: $appointmentId")
                viewModel.checkAppointmentStatus(appointmentId) { isAccepted ->
                    activity?.runOnUiThread {
                        when (isAccepted) {
                            true -> {
                                tvStatus.text = "Accepted"
                                showSnackbar("Appointment was already accepted.")
                                hideButtons()  // No rebooking option
                            }
                            false -> {
                                tvStatus.text = "Declined"
                                showSnackbar("Appointment was already declined.")
                                hideButtons()  // No rebooking option
                            }
                            null -> {
                                tvStatus.text = "For Confirmation"
                                showButtons()
                            }
                        }
                    }
                }
            } else {
                Log.e("AppointmentRequestFragment", "Appointment ID is blank")
                showSnackbar("Error: Invalid appointment ID")
                showButtons()
            }
        } ?: run {
            Log.e("AppointmentRequestFragment", "No appointment ID in arguments")
            showSnackbar("Error: Missing appointment ID")
            showButtons()
        }
    }

    private fun setupClickListeners() {
        acceptBtn.setOnClickListener {
            handleAppointmentResponse(true)
        }

        rejectBtn.setOnClickListener {
            handleAppointmentResponse(false)
        }
    }

    private fun hideButtons() {
        acceptBtn.visibility = View.GONE
        rejectBtn.visibility = View.GONE
    }

    private fun showButtons() {
        acceptBtn.visibility = View.VISIBLE
        rejectBtn.visibility = View.VISIBLE
    }

    private fun handleAppointmentResponse(isAccepted: Boolean) {
        arguments?.let { args ->
            val appointmentId = args.getString("appointmentId", "")
            // Log the appointmentId being handled
            Log.d("AppointmentRequestFragment", "Handling response for appointmentId: $appointmentId")

            if (isAccepted) {
                viewModel.acceptAppointment(appointmentId)
            } else {
                viewModel.rejectAppointment(appointmentId)
            }
            hideButtons()
        } ?: run {
            Log.e("AppointmentRequestFragment", "No arguments provided when handling response")
            showSnackbar("Error: Could not process response")
        }
    }

    private fun observeResponseStatus() {
        viewModel.appointmentResponseStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is AppointmentResponseStatus.Success -> {
                    showSnackbar(
                        if (status.isAccepted) "Appointment accepted successfully"
                        else "Appointment declined successfully"
                    )

                    // Update status text based on response
                    tvStatus.text = if (status.isAccepted) "Accepted" else "Declined"

                    hideButtons()
                    navigateBack()
                }
                is AppointmentResponseStatus.Error -> {
                    showSnackbar("Error: ${status.message}")
                    showButtons()
                }
                is AppointmentResponseStatus.Loading -> {
                }
                null -> {
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }
}

// Status sealed class for appointment response
sealed class AppointmentResponseStatus {
    object Loading : AppointmentResponseStatus()
    data class Success(val isAccepted: Boolean) : AppointmentResponseStatus()
    data class Error(val message: String) : AppointmentResponseStatus()
}
