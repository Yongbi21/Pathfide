package com.example.pathfide.models

data class Question(
    val id: String,
    val textResId: Int,
    val branching: Map<String, String>?
)