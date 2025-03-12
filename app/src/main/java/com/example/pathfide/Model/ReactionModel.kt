package com.example.pathfide.Model

data class Reaction(
    val id: String,
    val userId: String,
    val username: String,
    val type: ReactionType
)

enum class ReactionType {
    LIKE, LOVE
}