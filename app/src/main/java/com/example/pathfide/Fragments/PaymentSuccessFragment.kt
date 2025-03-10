package com.example.pathfide.Fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R

class PaymentSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_success, container, false)

        // Find the Done button
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        // Navigate back to HomeFragment when Done is clicked
        btnDone.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        return view
    }
}
