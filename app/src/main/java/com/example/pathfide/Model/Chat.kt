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
        var avatarUrl: String = "",
        var firstName: String? = null,
        var surName: String? = null,
        var lastName: String? = null,
        val profileImage: String = "",
        val userId: String = "",
        var user: User? = null,
        val isSystemMessage: Boolean = false,
        val formattedTime: String = ""

    )