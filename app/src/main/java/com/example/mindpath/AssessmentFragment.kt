//package com.example.mindpath.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import com.example.mindpath.ViewModel.AssessmentViewModel
//import com.example.mindpath.R
//import com.example.mindpath.fragments.assessment.FinalAssessmentFragment
//import com.example.mindpath.fragments.assessment.QuestionFragment
//
//class AssessmentFragment : Fragment() {
//    private lateinit var viewModel: AssessmentViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        viewModel = ViewModelProvider(requireActivity()).get(AssessmentViewModel::class.java)
//
//        val view = inflater.inflate(R.layout.fragment_assesment, container, false)
//
//
//        // Reset the assessment when navigating to this fragment
//        viewModel.resetAssessment()
//
//        // Load the first question fragment if not already loaded
//        if (savedInstanceState == null) {
//            loadQuestionFragment()
//        }
//
//
//        // Observe the current question ID to load appropriate fragments
//        viewModel.currentQuestionId.observe(viewLifecycleOwner) { questionId ->
//            if (questionId == null) {
//                loadFinalAssessmentFragment()
//            } else {
//                loadQuestionFragment()
//            }
//        }
//
//        // Observe answer changes to automatically progress
//        viewModel.currentAnswer.observe(viewLifecycleOwner) { answer ->
//            if (!answer.isNullOrEmpty()) {
//                handleAnswerSelected()
//            }
//        }
//
//        // Observe if the assessment is complete
//        viewModel.isAssessmentComplete.observe(viewLifecycleOwner) { isComplete ->
//            if (isComplete) {
//                loadFinalAssessmentFragment()
//            }
//        }
//
//
//        return view
//    }
//
//    private fun handleAnswerSelected() {
//        if (viewModel.isLastQuestion()) {
//            loadFinalAssessmentFragment()
//        } else {
//            viewModel.moveToNextQuestion()
//        }
//    }
//
//    // Function to load the question fragment
//    private fun loadQuestionFragment() {
//        childFragmentManager.beginTransaction()
//            .replace(R.id.questionContainer, QuestionFragment())
//            .commit()
//    }
//
//    // Function to load the final assessment fragment
//    private fun loadFinalAssessmentFragment() {
//        childFragmentManager.beginTransaction()
//            .replace(R.id.questionContainer, FinalAssessmentFragment())
//            .commit()
//    }
//}
