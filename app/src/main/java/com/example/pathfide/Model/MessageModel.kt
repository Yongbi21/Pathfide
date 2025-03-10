package com.example.pathfide.Model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val content: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    var formattedTime: String = "",  // New field to hold the formatted time

    @ServerTimestamp
    val timestamp: Date? = null,
    val type: String = "text"
)


