package com.example.pathfide.Model

data class Notification(
    val id: String,
    val message: String,
    val isAccepted: Boolean,
    val timestamp: Long = System.currentTimeMillis()

)