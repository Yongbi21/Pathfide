package com.example.pathfide.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.Activities.LanguageSettingsActivity
import com.example.pathfide.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailInputFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var settingsButton: Button
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email_input, container, false)

        auth = Firebase.auth

        emailEditText = view.findViewById(R.id.forgotEmail)
        continueButton = view.findViewById(R.id.continueForgotPassword)
        backButton = view.findViewById(R.id.backButton)
        settingsButton = view.findViewById(R.id.settingsButton)

        continueButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (isEmailValid(email)) {
                sendPasswordResetEmail(email)
            } else {
                emailEditText.error = "Please enter a valid email address"
            }
        }

        // Handle Settings Button Click
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), LanguageSettingsActivity::class.java)
            startActivity(intent)
        }

        // Handle Back Button Click
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    private fun isEmailValid(email: String): Boolean {
        // Basic email validation regex
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_emailInputFragment_to_confirmationFragment)
                } else {
                    Toast.makeText(context, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}