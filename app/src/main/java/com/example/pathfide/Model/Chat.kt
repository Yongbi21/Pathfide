    package com.example.pathfide.Model

    import com.google.firebase.firestore.ServerTimestamp
    import java.util.Date

    data class Chat(
        val id: String = "",
        val name: String = "",
        val participants: List<String> = listOf(),
        val lastMessage: String = "",
        @ServerTimestamp
        val lastMessageTimestamp: Date? = null,
        val avatarUrl: String = "",
        var firstName: String? = null,
        var lastName: String? = null,
        val profileImage: String = "",
        val userId: String = "",
        var user: User? = null, // Add this field to hold user details
        val isSystemMessage: Boolean = false,

    )