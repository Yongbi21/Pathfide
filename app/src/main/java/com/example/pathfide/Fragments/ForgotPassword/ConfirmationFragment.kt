package com.example.pathfide.Fragments.ForgotPassword

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.pathfide.Activities.SigninActivity
import com.example.pathfide.R

class ConfirmationFragment : Fragment() {

    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_confirmation, container, false)

        loginButton = view.findViewById(R.id.loginNewpassword)

        loginButton.setOnClickListener {
            // Redirect to SignInActivity
            val intent = Intent(activity, SigninActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
