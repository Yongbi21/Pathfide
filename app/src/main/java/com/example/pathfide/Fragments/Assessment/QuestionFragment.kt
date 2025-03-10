package com.example.pathfide.fragments.assessment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pathfide.ViewModel.AssessmentViewModel
import com.example.pathfide.R
import com.example.pathfide.utils.QuestionManager

class QuestionFragment : Fragment() {
    private lateinit var viewModel: AssessmentViewModel
    private lateinit var radioGroup: RadioGroup
    private lateinit var questionTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(requireActivity()).get(AssessmentViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_question, container, false)

        questionTextView = view.findViewById(R.id.cardTextView)
        radioGroup = view.findViewById(R.id.answerLayout)
        progressBar = view.findViewById(R.id.progressBar) // Initialize progress bar
        titleTextView = view.findViewById((R.id.getAssessedTitle))

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedAnswer = view.findViewById<RadioButton>(checkedId).text.toString()
            viewModel.saveAnswer(selectedAnswer)
        }

        viewModel.currentQuestionId.observe(viewLifecycleOwner) { questionId ->
            updateQuestion(questionId)
        }

        // Observe progress and update progress bar
        viewModel.currentProgress.observe(viewLifecycleOwner) { progress ->
            progressBar.progress = progress // Set the progress value
        }

        val infoIcon: ImageView = view.findViewById(R.id.infoIcon)

        // Show tooltip when clicking the info icon
        infoIcon.setOnClickListener {
            showTooltip(it, "Kessler R. Professor of Health Care Policy, Harvard Medical School, Boston, USA.")
        }


        return view
    }

    private fun showTooltip(anchorView: View, message: String) {
        val inflater = LayoutInflater.from(anchorView.context)
        val tooltipView = inflater.inflate(R.layout.tooltip_layout, null)

        val popupWindow = PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set tooltip text
        tooltipView.findViewById<TextView>(R.id.tooltipText).text = message

        // Set background (optional)
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(anchorView.context, R.drawable.tooltip_background))

        // Show the tooltip near the icon
        popupWindow.showAsDropDown(anchorView, 0, -anchorView.height - 10, Gravity.CENTER)
    }

    private fun updateQuestion(questionId: String) {
        val question = QuestionManager.getQuestion(questionId)
        if (question != null) {
            questionTextView.setText(question.textResId) // Set localized question text
        }

        // Update radio button options dynamically
        val options = QuestionManager.getLocalizedRadioOptions()
        if (radioGroup.childCount >= options.size) {
            for (i in options.indices) {
                (radioGroup.getChildAt(i) as? RadioButton)?.setText(options[i]) // Set localized text
            }
        }

        // Update "Get Assessed" title dynamically
        titleTextView.setText(QuestionManager.getLocalizedGetAssessedTitle())
    }
}
