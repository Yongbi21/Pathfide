package com.example.pathfide.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.R
import com.example.pathfide.databinding.FragmentSelfcareTipsBinding

class SelfCareTipsFragment : Fragment() {

    private var _binding: FragmentSelfcareTipsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfcareTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.stressnAnxiety.setOnClickListener {
            try {
                findNavController().navigate(R.id.stressFragment)
            } catch (e: Exception) {
                Log.e("SelfCareTipsFragment", "Error navigating to StressAnxietyFragment", e)
            }
        }

        binding.selfEsteem.setOnClickListener {
            try {
                findNavController().navigate(R.id.selfEsteemFragment)
            } catch (e: Exception) {
                Log.e("SelfCareTipsFragment", "Error navigating to SelfEsteemFragment", e)
            }
        }

        binding.Meditation.setOnClickListener {
            try {
                findNavController().navigate(R.id.meditationFragment)
            } catch (e: Exception) {
                Log.e("SelfCareTipsFragment", "Error navigating to MindfulnessMeditationFragment", e)
            }
        }
        binding.socialConnection.setOnClickListener {
            try {
                findNavController().navigate(R.id.ConnectionFragment)
            } catch (e: Exception) {
                Log.e("SelfCareTipsFragment", "Error navigating to MindfulnessMeditationFragment", e)
            }
        }
        binding.copeLoss.setOnClickListener {
            try {
                findNavController().navigate(R.id.CopingFragment)
            } catch (e: Exception) {
                Log.e("SelfCareTipsFragment", "Error navigating to MindfulnessMeditationFragment", e)
            }
        }
        binding.mindful.setOnClickListener {
            try {
                findNavController().navigate(R.id.MindfulFragment)
            } catch (e: Exception) {
                Log.e("SelfCareTipsFragment", "Error navigating to MindfulnessMeditationFragment", e)
            }
        }
    }

}
