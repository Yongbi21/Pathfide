package com.example.pathfide.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    @DocumentId val id: String = "",
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val content: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null,
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var isLiked: Boolean = false,
    var likedBy: MutableList<String> = mutableListOf(),
    var profileImageUrl: String = ""
)
{
    constructor() : this("", "", "", "", "")
}