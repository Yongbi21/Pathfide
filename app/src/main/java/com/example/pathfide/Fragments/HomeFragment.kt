package com.example.pathfide.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.example.pathfide.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Tag for logging
    private val TAG = "HomeFragment"
    private val quotes = listOf(
        "Always remember, it is okay to ask for help if you are struggling",
        "Self-care is how you take your power back.",
        "Progress is progress, no matter how small.",
        "You're allowed to take up space. You're allowed to be who you are.",
        "It's okay to need a break. It's okay to take time for yourself.",
        "You are enough, just as you are.",
        "It's okay to ask for help. You don't have to do everything alone.",
        "Your feelings are valid. Your experiences matter.",
        "Your mental health is a priority. Always take care of yourself",
        "You are worthy of love and respect. Always remember that.",
        "You are stronger than you think",
        "It's okay not to be okay sometimes",
        "Small steps are still progress",
        "Be gentle with yourself, you're doing the best you can"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()  // Set up listeners for UI actions
        setRandomQuote() // Set a random quote when the fragment is created

    }

    /**
     * Set up click listeners for various card views and the search view.
     */
    private fun setupListeners() {
        binding.apply {

            assessmentCard.setOnClickListener {
                Log.d(TAG, "Card Clicked")
                val startTime = System.currentTimeMillis()

                navigateToFragment(R.id.action_homeFragment_to_assessmentFragment)

                Log.d(TAG, "Navigation requested after ${System.currentTimeMillis() - startTime} ms")
            }

            // Navigate to Seek Professional Help Fragment
            seekHelpCard.setOnClickListener {
                val startTime = System.currentTimeMillis()

                navigateToFragment(R.id.action_homeFragment_to_seekHelpFragment)

                Log.d(TAG, "Navigation requested after ${System.currentTimeMillis() - startTime} ms")

            }

            // Navigate to Self-Care Tips Fragment
            selfCareTipsCard.setOnClickListener {
                Log.d(TAG, "Card Clicked")
                val startTime = System.currentTimeMillis()

                navigateToFragment(R.id.action_homeFragment_to_selfCareTipsFragment)

                Log.d(TAG, "Navigation requested after ${System.currentTimeMillis() - startTime} ms")

            }

            // Navigate to Threads Fragment
            threadsCard.setOnClickListener {
                Log.d(TAG, "Card Clicked")
                val startTime = System.currentTimeMillis()

                navigateToFragment(R.id.action_homeFragment_to_threadsFragment)
                Log.d(TAG, "Navigation requested after ${System.currentTimeMillis() - startTime} ms")

            }

            // Navigate to Mood Tracker Fragment
            moodTrackerCard.setOnClickListener {
                Log.d(TAG, "Card Clicked")
                val startTime = System.currentTimeMillis()

                navigateToFragment(R.id.action_homeFragment_to_moodTrackerFragment)
                Log.d(TAG, "Navigation requested after ${System.currentTimeMillis() - startTime} ms")

            }

            // Navigate to Careline Fragment
            careLineCard.setOnClickListener {
                Log.d(TAG, "Card Clicked")
                val startTime = System.currentTimeMillis()
                navigateToFragment(R.id.action_homeFragment_to_careLineFragment)
                Log.d(TAG, "Navigation requested after ${System.currentTimeMillis() - startTime} ms")

            }

            // Navigate to Chat Fragment
            chatPageIcon.setOnClickListener {
                try {
                    navigateToFragment(R.id.action_homeFragment_to_chatMainFragment)
                } catch (e: Exception) {
                    Log.d("Chat Fragment", "Toolbar set as ActionBar")
                }
            }

        }
    }

    /**
     * Generic method for navigating to another fragment using action ID.
     */
    private fun navigateToFragment(actionId: Int) {
        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed: ${e.message}")
        }
    }



    // New method to set a random quote
    private fun setRandomQuote() {
        val randomQuote = quotes.random()
        binding.randomQoutesView.text = randomQuote
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
