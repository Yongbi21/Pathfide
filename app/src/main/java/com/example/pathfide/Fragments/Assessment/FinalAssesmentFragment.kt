package com.example.pathfide.fragments.assessment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pathfide.ViewModel.AssessmentViewModel
import com.example.pathfide.R

class FinalAssessmentFragment : Fragment() {
    private lateinit var viewModel: AssessmentViewModel
    private lateinit var summaryTextView: TextView
    private lateinit var homeButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(AssessmentViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_final_assesment, container, false)

        summaryTextView = view.findViewById(R.id.summaryTextView)
        homeButton = view.findViewById(R.id.btnDone)

        val answers = viewModel.getAnswers()
        Log.d("FinalAssessmentFragment", "Answers: $answers")  // Log the answers

        val overallScore = calculateOverallScore(answers)

        // Display the advice based on the overall score
        summaryTextView.text = getFinalAdvice(overallScore)

        homeButton.setOnClickListener {
            viewModel.resetAssessment()
            findNavController().navigate(R.id.homeFragment)
        }

        return view
    }

    private fun getAnswerScore(answer: String): Int {
        return when (answer) {
            "Never" -> 5
            "Rarely" -> 4
            "Sometimes" -> 3
            "Often" -> 2
            "Always" -> 1
            else -> 0
        }
    }

    private fun calculateOverallScore(answers: List<String>): Int {
        return answers.map { getAnswerScore(it) }.sum()
    }

    private fun getFinalAdvice(overallScore: Int): String {
        Log.d("FinalAssessmentFragment", "Overall Score: $overallScore")  // Log the overall score
        return when {
            overallScore >= 45 -> getString(R.string.outcome6f)  // Very Good
            overallScore >= 35 -> getString(R.string.outcome6f2)  // Good
            overallScore >= 25 -> getString(R.string.outcome6f3)  // Neutral
            else -> getString(R.string.outcome6f4)  // Bad
        }
    }
}