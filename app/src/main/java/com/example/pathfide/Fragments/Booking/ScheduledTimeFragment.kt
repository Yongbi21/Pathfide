    package com.example.pathfide.Fragments.Booking

    import android.app.DatePickerDialog
    import android.app.TimePickerDialog
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.annotation.RequiresApi
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.pathfide.Adapter.BookingNoticeAdapter
    import com.example.pathfide.R
    import com.example.pathfide.ViewModel.SharedViewModel
    import com.example.pathfide.databinding.FragmentScheduledTimeBinding
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FieldValue
    import com.google.firebase.firestore.FirebaseFirestore
    import java.time.LocalDate
    import java.time.LocalTime
    import java.time.format.DateTimeFormatter


    class ScheduledTimeFragment : Fragment() {
        private var _binding: FragmentScheduledTimeBinding? = null
        private val binding get() = _binding!!
        private lateinit var datePickerDialog: DatePickerDialog
        private lateinit var timePickerDialog: TimePickerDialog
        private var selectedDate: LocalDate? = null
        private var selectedTime: LocalTime? = null
        private val auth = FirebaseAuth.getInstance()
        private val db = FirebaseFirestore.getInstance()
        private var selectedSessionType: String? = null
        private var userFirstName: String? = null
        private var userLastName: String? = null
        private var middleName: String? = null
        private var gender: String? = null
        private var birthdate: String? = null
        private var contactNumber: String? = null
        private lateinit var bookingNoticeAdapter: BookingNoticeAdapter
        private val bookingList: MutableList<String> = mutableListOf()
        private val sharedViewModel: SharedViewModel by activityViewModels()
        private var selectedTherapistId: String? = null




        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentScheduledTimeBinding.inflate(inflater, container, false)

            // Initialize the RecyclerView and its adapter
            bookingNoticeAdapter = BookingNoticeAdapter(bookingList)
            binding.bookingNoticeRecyclerView.adapter = bookingNoticeAdapter
            binding.bookingNoticeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.bookingNoticeRecyclerView.visibility = View.GONE

            sharedViewModel.userId.observe(viewLifecycleOwner) { userId ->
                selectedTherapistId = userId
            }
            fetchUserSessionData()
            fetchUserProfileData()

            initDatePicker()
            showDatePicker()
            fetchSelectedTherapistId()


    //        userId in the sharedviewmodel throws null instead of retrieving


            binding.btnConfirm.setOnClickListener {
                navigateToConfirmation()
            }

            return binding.root
        }



        @RequiresApi(Build.VERSION_CODES.O)
        private fun initDatePicker() {
            val currentDate = LocalDate.now()

            datePickerDialog = DatePickerDialog(
                requireContext(),
                null,
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).apply {
                setCancelable(false) // Prevent closing when touching outside
                setCanceledOnTouchOutside(false) // Prevent closing on outside touch
            }

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()

            datePickerDialog.setOnShowListener {
                val button = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    val dayOfMonth = datePickerDialog.datePicker.dayOfMonth
                    val month = datePickerDialog.datePicker.month
                    val year = datePickerDialog.datePicker.year

                    handleDateSelection(year, month, dayOfMonth)
                }

                // Set up the Cancel button to navigate back to the previous fragment
                val cancelButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                cancelButton.setOnClickListener {
                    datePickerDialog.dismiss()
                    // Navigate back to the previous fragment
                    parentFragmentManager.popBackStack()
                }
            }

            showDatePicker()
        }



        // Function to handle date selection logic
        @RequiresApi(Build.VERSION_CODES.O)
        private fun handleDateSelection(year: Int, month: Int, dayOfMonth: Int) {
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)

            // Check if the selected date is a weekend (Saturday or Sunday)
            if (selectedDate?.dayOfWeek?.value == 6 || selectedDate?.dayOfWeek?.value == 7) {
                showError("Please select a weekday. Scheduling on weekends is not allowed.")
            } else {
                datePickerDialog.dismiss()
                showTimePicker()
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        private fun handleTimeSelection() {
            selectedDate?.let { date ->
                selectedTime?.let { time ->
                    sharedViewModel.fullName.observe(viewLifecycleOwner) { fullName ->
                        // Format date and time
                        val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val formattedTime = time.format(DateTimeFormatter.ofPattern("h:mm a"))

                        val bookingTitle = "You've booked an online session with $fullName on $formattedDate at $formattedTime"
                        val fullBookingDetails = bookingTitle

                        bookingNoticeAdapter.addBooking(fullBookingDetails)
                        binding.bookingNoticeRecyclerView.visibility = View.VISIBLE

                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun showTimePicker() {
            val currentTime = LocalTime.now()
            timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    // Handle time selection
                    selectedTime = LocalTime.of(hourOfDay, minute)
                    handleTimeSelection()
                },
                currentTime.hour,
                currentTime.minute,
                false // 12-hour format
            ).apply {
                setCancelable(false) // Prevent closing when touching outside
                setCanceledOnTouchOutside(false)
            }

            // Handle dismiss action to navigate back if no time is selected
            timePickerDialog.setOnDismissListener {
                if (selectedTime == null) { // Only navigate if no time was selected
                    parentFragmentManager.popBackStack()
                }
            }

            timePickerDialog.show()
        }

        private fun showDatePicker() {
            datePickerDialog.show()
        }

        private fun showError(message: String) {
            // Show an error message (use a Toast, Snackbar, or any UI component)
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }


        @RequiresApi(Build.VERSION_CODES.O)
        private fun navigateToConfirmation() {
            if (selectedDate != null && selectedTime != null) {
                val formattedDate = selectedDate.toString()
                val formattedTime = selectedTime?.format(DateTimeFormatter.ofPattern("h:mm a"))

                saveScheduledTimeToFirestore(formattedDate, formattedTime ?: "")
                findNavController().navigate(R.id.action_scheduledTimeFragment_to_homeFragment)

            } else {
                showError("Please select a date and time before confirming.")
            }
        }


        private fun fetchUserSessionData() {
            val userId = auth.currentUser?.uid

            if (userId != null) {
                db.collection("sessions").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            selectedSessionType = document.getString("sessionType")
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
            }
        }

        private fun fetchUserProfileData() {
            val userId = auth.currentUser?.uid

            if (userId != null) {
                db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Retrieve the profile details and assign them to class variables
                            userFirstName = document.getString("firstName")
                            userLastName = document.getString("lastName")
                            middleName = document.getString("middleName")
                            gender = document.getString("gender")
                            birthdate = document.getString("birthdate")
                            contactNumber = document.getString("contactNumber")
                        } else {
                            // Handle case where user document does not exist
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
            }
        }

        private fun saveScheduledTimeToFirestore(date: String, time: String) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Prepare data with additional fields
                val scheduledData = hashMapOf(
                    "appointmentDate" to date,
                    "appointmentTime" to time,
                    "sessionType" to selectedSessionType,
                    "firstName" to userFirstName,
                    "lastName" to userLastName,
                    "middleName" to middleName,
                    "gender" to gender,
                    "birthdate" to birthdate,
                    "contactNumber" to contactNumber,
                    "therapistId" to sharedViewModel.userId.value,
                    "clientId" to userId,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "isRebooking" to (selectedTherapistId != null),
                    "therapistName" to sharedViewModel.fullName.value,
                    "status" to "Pending"


                )

                // Save data to Firestore
                db.collection("scheduledSessions")
                    .add(scheduledData)
                    .addOnSuccessListener { documentRef ->
                        val appointmentId = documentRef.id
                        context?.let {
                            Toast.makeText(it, "Booking confirmed!", Toast.LENGTH_SHORT).show()
                        }
                        // Add notification after successfully saving the scheduled time
                        addNotification(userId, "You've booked a session on $date at $time", appointmentId)

                        // Update the document with its own ID
                        documentRef.update("appointmentId", appointmentId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error saving scheduled time", e)
                        addNotification(userId, "Failed to book a session on $date at $time", null)
                    }
            }
        }

        private fun addNotification(userId: String, message: String, appointmentId: String?) {
            // Notification for Therapist
            val therapistNotification = hashMapOf(
                "therapistId" to sharedViewModel.userId.value,
                "clientId" to userId,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp(),
                "isReadClient" to false,
                "isReadTherapist" to false,
                "userType" to "THERAPIST",
                "appointmentId" to appointmentId
            )

            db.collection("notifications")
                .add(therapistNotification)
                .addOnSuccessListener {
                    Log.d("NotificationManager", "Notification added successfully for therapist.")
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationManager", "Error adding notification for therapist", e)
                }

            // Notification for Client
            val clientNotification = hashMapOf(
                "therapistId" to sharedViewModel.userId.value,
                "clientId" to userId,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp(),
                "isReadClient" to false,
                "isReadTherapist" to false,
                "userType" to "CLIENT",
                "appointmentId" to appointmentId
            )

            db.collection("notifications")
                .add(clientNotification)
                .addOnSuccessListener {
                    Log.d("NotificationManager", "Notification added successfully for client.")
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationManager", "Error adding notification for client", e)
                }
        }




        private fun fetchSelectedTherapistId() {
            val userId = auth.currentUser?.uid

            if (userId != null) {
                db.collection("sessions").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            selectedTherapistId = document.getString("therapistId")
                            Log.d("ScheduledTimeFragment", "Fetched therapist ID: $selectedTherapistId")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ScheduledTimeFragment", "Error fetching therapist ID", e)
                    }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
