package com.example.pathfide.Fragments.Booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.example.pathfide.databinding.FragmentBookingOptionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookingOptionFragment : Fragment() {

    private var _binding: FragmentBookingOptionBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingOptionBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set click listener on the "Next" button
        binding.radioGroupBookingFor.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbIamPatient -> findNavController().navigate(R.id.action_bookingOptionFragment_to_bookingMethodFragment)
                R.id.rbBookForSomeoneElse -> findNavController().navigate(R.id.action_bookingOptionFragment_to_bookingFormFragment)
            }
        }


        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
