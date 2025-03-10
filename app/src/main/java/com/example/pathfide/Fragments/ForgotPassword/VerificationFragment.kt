//package com.example.mindpath.Fragments
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.example.mindpath.Activities.LanguageSettingsActivity
//import com.example.mindpath.R
//
//class VerificationFragment : Fragment() {
//
//    private lateinit var box1: EditText
//    private lateinit var box2: EditText
//    private lateinit var box3: EditText
//    private lateinit var box4: EditText
//    private lateinit var continueButton: Button
//    private lateinit var settingsButton: Button
//    private lateinit var backButton: ImageView
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_verification, container, false)
//
//
//        backButton = view.findViewById(R.id.backButton)
//        settingsButton = view.findViewById(R.id.settingsButton)
//        box1 = view.findViewById(R.id.box1)
//        box2 = view.findViewById(R.id.box2)
//        box3 = view.findViewById(R.id.box3)
//        box4 = view.findViewById(R.id.box4)
//        continueButton = view.findViewById(R.id.continueForgotPassword)
//
//        continueButton.setOnClickListener {
//            // Assuming all code boxes are filled
//            if (isCodeValid()) {
//                // Navigate to the NewPasswordFragment using action ID directly
//                findNavController().navigate(R.id.action_verificationFragment_to_newPasswordFragment)
//            } else {
//                // Show error message
//                Toast.makeText(context, "Please fill all code boxes", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Handle Settings Button Click
//        settingsButton.setOnClickListener {
//            val intent = Intent(requireContext(), LanguageSettingsActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Handle Back Button Click
//        backButton.setOnClickListener {
//            requireActivity().onBackPressed()
//        }
//        return view
//    }
//
//    private fun isCodeValid(): Boolean {
//        return box1.text.isNotEmpty() && box2.text.isNotEmpty() &&
//                box3.text.isNotEmpty() && box4.text.isNotEmpty()
//    }
//}
