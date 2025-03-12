package com.example.pathfide.Fragments.Booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.example.pathfide.databinding.FragmentBookingFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookingFormFragment : Fragment() {

    private var _binding: FragmentBookingFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingFormBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                saveDataToFirestore()
                navigateToNextFragment()
            }
        }

        return binding.root
    }

    private fun validateInputs(): Boolean {
        val firstName = binding.firstNameProfile.text.toString().trim()
        val lastName = binding.lastNameProfile.text.toString().trim()
        val ageInput = binding.birthdateProfile.text.toString().trim()
        val gender = binding.genderSpinner2.selectedItem.toString()
        val relation = binding.relationSpinner.selectedItem.toString()

        // Check for empty fields and validate
        if (firstName.isEmpty()) {
            Toast.makeText(requireContext(), "First Name is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Last Name is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isValidName(firstName)) {
            Toast.makeText(requireContext(), "First Name cannot contain numbers or special characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isValidName(lastName)) {
            Toast.makeText(requireContext(), "Last Name cannot contain numbers or special characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (ageInput.isEmpty()) {
            Toast.makeText(requireContext(), "Age is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isNumeric(ageInput)) {
            Toast.makeText(requireContext(), "Age must be a number", Toast.LENGTH_SHORT).show()
            return false
        }

        // Now, safely convert ageInput to an Integer
        val age = ageInput.toIntOrNull()
        if (age == null || age < 0 || age > 120) {
            Toast.makeText(requireContext(), "Please enter a valid age between 0 and 120", Toast.LENGTH_SHORT).show()
            return false
        }
        if (gender == "Select Gender") { // Assuming "Select Gender" is the default option
            Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
            return false
        }
        if (relation == "Select Relationship") { // Assuming "Select Relationship" is the default option
            Toast.makeText(requireContext(), "Please select a relationship", Toast.LENGTH_SHORT).show()
            return false
        }

        return true // All validations passed
    }

    private fun isValidName(name: String): Boolean {
        return name.all { it.isLetter() || it.isWhitespace() }
    }

    private fun isNumeric(str: String): Boolean {
        return str.all { it.isDigit() }
    }

    private fun saveDataToFirestore() {
        val firstName = binding.firstNameProfile.text.toString().trim()
        val lastName = binding.lastNameProfile.text.toString().trim()
        val middleName = binding.middleNameProfile.text.toString().trim()
        val gender = binding.genderSpinner2.selectedItem.toString()
        val age = binding.birthdateProfile.text.toString().trim()
        val relation = binding.relationSpinner.selectedItem.toString()

        val bookingData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "middleName" to middleName,
            "gender" to gender,
            "age" to age,
            "relation" to relation
        )

        // Replace "bookings" with your Firestore collection name
        db.collection("occupation")
            .add(bookingData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToNextFragment() {
        // Navigate to the next fragment (replace with your destination)
        findNavController().navigate(R.id.action_bookingFormFragment_to_bookingMethodFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
