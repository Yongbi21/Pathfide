package com.example.pathfide.Fragments.Booking

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.example.pathfide.databinding.FragmentBookingMethodBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookingMethodFragment : Fragment() {

    private var _binding: FragmentBookingMethodBinding? = null
    private val binding get() = _binding!!

    private var selectedCard: CardView? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("BookingMethodFragment", "onCreateView: Fragment View is being created")
        _binding = FragmentBookingMethodBinding.inflate(inflater, container, false)

        // Reset selection on fragment creation
        selectedCard = null
        binding.onsiteSession.setCardBackgroundColor(Color.WHITE)
        binding.onlineSession.setCardBackgroundColor(Color.WHITE)

        // Set up automatic navigation when an option is selected
        setupCardClickListeners()

        // Handle back button manually
        setupBackButtonHandler()

        return binding.root
    }

    /**
     * Handles back button press manually to ensure proper navigation.
     */
    private fun setupBackButtonHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d("BookingMethodFragment", "handleOnBackPressed: Back button pressed")

                    if (findNavController().currentDestination?.id == R.id.bookingMethodFragment) {
                        Log.d("BookingMethodFragment", "Navigating back using popBackStack()")
                        val result = findNavController().popBackStack()
                        Log.d("BookingMethodFragment", "popBackStack() result: $result")
                    } else {
                        Log.d("BookingMethodFragment", "Back navigation disabled, delegating to system")
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )
    }


    /**
     * Sets up click listeners for selecting a session type.
     */
    private fun setupCardClickListeners() {
        Log.d("BookingMethodFragment", "setupCardClickListeners: Setting up click listeners")
        binding.onsiteSession.setOnClickListener {
            Log.d("BookingMethodFragment", "Onsite session clicked")
            navigateAfterSelection(binding.onsiteSession, "Onsite Session")
        }

        binding.onlineSession.setOnClickListener {
            Log.d("BookingMethodFragment", "Online session clicked")
            navigateAfterSelection(binding.onlineSession, "Online Session")
        }
    }

    /**
     * Handles navigation after selecting a session.
     */
    private fun navigateAfterSelection(card: CardView, sessionType: String) {
        try {
            Log.d("BookingMethodFragment", "navigateAfterSelection: Selected session - $sessionType")

            // Reset previous selection
            selectedCard?.setCardBackgroundColor(Color.WHITE)
            selectedCard = card

            // Change the selected card's background color
            selectedCard?.setCardBackgroundColor(Color.LTGRAY)

            // Save session type to Firestore
            saveSessionTypeToFirestore(sessionType)

            // Ensure navigation only happens if we're in the correct fragment
            if (findNavController().currentDestination?.id == R.id.bookingMethodFragment) {
                findNavController().navigate(
                    R.id.action_bookingMethodFragment_to_scheduledTimeFragment,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.bookingMethodFragment, true)
                        .setLaunchSingleTop(true)
                        .build()
                )
                Log.d("BookingMethodFragment", "Navigation to scheduledTimeFragment successful")
            } else {
                Log.d("BookingMethodFragment", "Navigation blocked: Wrong destination")
            }
        } catch (e: Exception) {
            Log.e("BookingMethodFragment", "Navigation error: ${e.message}")
        }
    }

    /**
     * Saves the selected session type to Firestore.
     */
    private fun saveSessionTypeToFirestore(sessionType: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("BookingMethodFragment", "Saving session type '$sessionType' for user: $userId")
            val sessionData = hashMapOf("sessionType" to sessionType)
            db.collection("sessions").document(userId)
                .set(sessionData)
                .addOnSuccessListener {
                    Log.d("BookingMethodFragment", "Session type saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("BookingMethodFragment", "Error saving session type: ${e.message}")
                    Toast.makeText(context, "Error saving session type: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("BookingMethodFragment", "User not logged in")
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("BookingMethodFragment", "onDestroyView: Fragment View is being destroyed")
        _binding = null
    }
}
