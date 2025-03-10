package com.example.pathfide.Model

data class Comment(
    var id: String = "",
    var postId: String = "",
    var userId: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var content: String = "",
    var profileImageUrl: String = "",
    var isLiked: Boolean = false,
    var likeCount: Int = 0,
    var likedBy: List<String> = emptyList(),
    var parentCommentId: String? = null,  // New field for reply feature
    var replies: List<Comment> = emptyList(), // New field for nested replies
    var timestamp: Long = System.currentTimeMillis() // Ensure timestamp is Long
)

