package com.example.pathfide.utils

import android.util.Log
import com.example.pathfide.R
import com.example.pathfide.models.Question

object QuestionManager {
    private var selectedLanguage: String = "english" // Default language

    // Define question keys for different languages
    private val questionKeys = mapOf(
        "1" to mapOf(
            "english" to R.string.question1f,
            "tagalog" to R.string.tagalogquestion1,
            "bisaya" to R.string.bisayaquestion1
        ),
        "2" to mapOf(
            "english" to R.string.question2f,
            "tagalog" to R.string.tagalogquestion2,
            "bisaya" to R.string.bisayaquestion2
        ),
        "3" to mapOf(
            "english" to R.string.question3f,
            "tagalog" to R.string.tagalogquestion3,
            "bisaya" to R.string.bisayaquestion3
        ),
        "4" to mapOf(
            "english" to R.string.question4f,
            "tagalog" to R.string.tagalogquestion4,
            "bisaya" to R.string.bisayaquestion4
        ),
        "5" to mapOf(
            "english" to R.string.question5f,
            "tagalog" to R.string.tagalogquestion5,
            "bisaya" to R.string.bisayaquestion5
        ),
        "6" to mapOf(
            "english" to R.string.question6f,
            "tagalog" to R.string.tagalogquestion6,
            "bisaya" to R.string.bisayaquestion6
        ),
        "7" to mapOf(
            "english" to R.string.question7f,
            "tagalog" to R.string.tagalogquestion7,
            "bisaya" to R.string.bisayaquestion7
        ),
        "8" to mapOf(
            "english" to R.string.question8f,
            "tagalog" to R.string.tagalogquestion8,
            "bisaya" to R.string.bisayaquestion8
        ),
        "9" to mapOf(
            "english" to R.string.question9f,
            "tagalog" to R.string.tagalogquestion9,
            "bisaya" to R.string.bisayaquestion9
        ),
        "10" to mapOf(
            "english" to R.string.question10f,
            "tagalog" to R.string.tagalogquestion10,
            "bisaya" to R.string.bisayaquestion10
        )
    )

    private val questions = mapOf(
        "1" to Question("1", R.string.question1f, mapOf(
            "Never" to "2",
            "Rarely" to "2",
            "Sometimes" to "2",
            "Often" to "2",
            "Always" to "2"
        )),
        "2" to Question("2", R.string.question2f, mapOf(
            "Never" to "3",
            "Rarely" to "3",
            "Sometimes" to "3",
            "Often" to "3",
            "Always" to "3"
        )),
        "3" to Question("3", R.string.question3f, mapOf(
            "Never" to "4",
            "Rarely" to "4",
            "Sometimes" to "4",
            "Often" to "4",
            "Always" to "4"
        )),
        "4" to Question("4", R.string.question4f, mapOf(
            "Never" to "5",
            "Rarely" to "5",
            "Sometimes" to "5",
            "Often" to "5",
            "Always" to "5"
        )),
        "5" to Question("5", R.string.question5f, mapOf(
            "Never" to "6",
            "Rarely" to "6",
            "Sometimes" to "6",
            "Often" to "6",
            "Always" to "6"
        )),
        "6" to Question("6", R.string.question6f, mapOf(
            "Never" to "7",
            "Rarely" to "7",
            "Sometimes" to "7",
            "Often" to "7",
            "Always" to "7"
        )),
        "7" to Question("7", R.string.question7f, mapOf(
            "Never" to "8",
            "Rarely" to "8",
            "Sometimes" to "8",
            "Often" to "8",
            "Always" to "8"
        )),
        "8" to Question("8", R.string.question8f, mapOf(
            "Never" to "9",
            "Rarely" to "9",
            "Sometimes" to "9",
            "Often" to "9",
            "Always" to "9"
        )),
        "9" to Question("9", R.string.question9f, mapOf(
            "Never" to "10",
            "Rarely" to "10",
            "Sometimes" to "10",
            "Often" to "10",
            "Always" to "10"
        )),
        "10" to Question("10", R.string.question10f, mapOf(
            "Never" to "finalAssessment",
            "Rarely" to "finalAssessment",
            "Sometimes" to "finalAssessment",
            "Often" to "finalAssessment",
            "Always" to "finalAssessment"
        )),
        "finalAssessment" to Question("finalAssessment", R.string.outcome6f, emptyMap())
    )

    private val getAssessedTitleKeys = mapOf(
        "english" to R.string.get_assessed_title_english,
        "tagalog" to R.string.get_assessed_title_tagalog,
        "bisaya" to R.string.get_assessed_title_bisaya
    )

    // Function to get localized title string ID
    fun getLocalizedGetAssessedTitle(): Int {
        return getAssessedTitleKeys[selectedLanguage]
            ?: throw IllegalArgumentException("Language not found for getAssessedTitle")
    }

    private val radioButtonOptions = mapOf(
        "english" to listOf(R.string.never, R.string.rarely, R.string.sometimes, R.string.often, R.string.always),
        "tagalog" to listOf(R.string.hindi_kailanman, R.string.bihira, R.string.kadalasan, R.string.madalas, R.string.laging),
        "bisaya" to listOf(R.string.walay_sukad, R.string.panagsa_ra, R.string.usahay, R.string.permi, R.string.kanunay)
    )

    fun getLocalizedRadioOptions(): List<Int> {
        return radioButtonOptions[selectedLanguage]
            ?: throw IllegalArgumentException("Language not found for radio buttons")
    }
    fun changeLanguage(language: String) {
        selectedLanguage = language
        Log.d("QuestionManager", "Language changed to: $selectedLanguage")
    }

    // Method to get the localized question string ID
    fun getLocalizedQuestionStringId(questionId: String): Int {
        return questionKeys[questionId]?.get(selectedLanguage)
            ?: throw IllegalArgumentException("Question ID or Language not found")
    }

    // Method to fetch the question object
    fun getQuestion(id: String): Question? {
        val question = questions[id]
        return if (question != null) {
            val localizedStringId = getLocalizedQuestionStringId(id)
            Question(id, localizedStringId, question.branching)
        } else {
            null
        }
    }

    fun getNextQuestionId(currentId: String, answer: String): String? {
        val nextId = questions[currentId]?.branching?.get(answer)
        return when {
            nextId != null -> nextId
            isLastQuestion(currentId) -> "finalAssessment"
            else -> null
        }
    }

    fun isLastQuestion(id: String): Boolean = id == "10"
}
