package com.example.pathfide.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfide.utils.QuestionManager

class AssessmentViewModel : ViewModel() {
    private val _currentQuestionId = MutableLiveData("1")
    val currentQuestionId: LiveData<String> = _currentQuestionId

    private val _currentAnswer = MutableLiveData<String?>()
    val currentAnswer: LiveData<String?> = _currentAnswer

    private val _isAssessmentComplete = MutableLiveData(false)
    val isAssessmentComplete: LiveData<Boolean> = _isAssessmentComplete

    private val _currentProgress = MutableLiveData(0)
    val currentProgress: LiveData<Int> = _currentProgress

    private val answers = mutableMapOf<String, String>()

    private val totalQuestions = 10 // Updated to match your question structure

    fun saveAnswer(answer: String) {
        val currentId = _currentQuestionId.value ?: return
        answers[currentId] = answer
        _currentAnswer.value = answer
        Log.d("AssessmentViewModel", "Answer saved for question $currentId: $answer")
    }

    fun moveToNextQuestion() {
        val currentId = _currentQuestionId.value ?: return
        val answer = _currentAnswer.value ?: return
        val nextQuestionId = QuestionManager.getNextQuestionId(currentId, answer)

        Log.d("AssessmentViewModel", "Current question ID: $currentId, Answer: $answer, Next question ID: $nextQuestionId")

        if (nextQuestionId != null) {
            _currentQuestionId.value = nextQuestionId
            _currentAnswer.value = null
            incrementProgress()
        } else {
            _isAssessmentComplete.value = true
        }
    }

    fun isLastQuestion(): Boolean {
        return _currentQuestionId.value == totalQuestions.toString()
    }

    private fun incrementProgress() {
        val newProgress = (_currentProgress.value ?: 0) + (100 / totalQuestions)
        Log.d("AssessmentViewModel", "Progress incremented to: $newProgress")
        _currentProgress.value = newProgress
    }

    fun getAnswers(): List<String> = answers.values.toList()

    fun resetAssessment() {
        answers.clear()
        _currentQuestionId.value = "1"
        _currentAnswer.value = null
        _isAssessmentComplete.value = false
        _currentProgress.value = 0
        Log.d("AssessmentViewModel", "Assessment state reset")
    }
}
