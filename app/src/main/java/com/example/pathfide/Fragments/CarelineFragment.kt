package com.example.pathfide.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.pathfide.R

class CarelineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_careline, container, false)

        // Find the backButton and set its click listener
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }
}