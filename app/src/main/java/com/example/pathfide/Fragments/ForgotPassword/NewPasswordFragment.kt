//package com.example.mindpath.Fragments.ForgotPassword
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.example.mindpath.Activities.LanguageSettingsActivity
//import com.example.mindpath.R
//
//class NewPasswordFragment : Fragment() {
//
//    private lateinit var newPasswordEditText: EditText
//    private lateinit var confirmPasswordEditText: EditText
//    private lateinit var continueButton: Button
//    private lateinit var settingsButton: Button
//    private lateinit var backButton: ImageView
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_new_password, container, false)
//
//
//        newPasswordEditText = view.findViewById(R.id.setnewPassword)
//        confirmPasswordEditText = view.findViewById(R.id.confirmnewPassword)
//        continueButton = view.findViewById(R.id.setnewPasswordButton)
//        backButton = view.findViewById(R.id.backButton)
//        settingsButton = view.findViewById(R.id.settingsButton)
//
//        continueButton.setOnClickListener {
//            if (isPasswordValid()) {
//                // Navigate to the SuccessFragment using action ID directly
//                findNavController().navigate(R.id.action_emailInputFragment_to_confirmationFragment_to_confirmationFragment)
//            } else {
//                // Show error message
//                confirmPasswordEditText.error = "Passwords do not match"
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
//
//        return view
//    }
//
//    private fun isPasswordValid(): Boolean {
//        val newPassword = newPasswordEditText.text.toString()
//        val confirmPassword = confirmPasswordEditText.text.toString()
//        return newPassword == confirmPassword && newPassword.length >= 8
//    }
//}
