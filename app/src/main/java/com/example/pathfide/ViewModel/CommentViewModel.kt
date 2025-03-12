package com.example.pathfide.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfide.Model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.*

class CommentViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    companion object {
        const val TAG = "CommentViewModel"
    }

    fun fetchComments(postId: String) {
        db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("CommentViewModel", "Error fetching comments: ${e.message}")
                    return@addSnapshotListener
                }

                val commentsList = mutableListOf<Comment>()
                snapshot?.documents?.forEach { document ->
                    val comment = document.toObject(Comment::class.java)
                    if (comment != null) {
                        comment.id = document.id

                        // Fetch the latest user details instead of using old data
                        db.collection("users").document(comment.userId)
                            .addSnapshotListener { userSnapshot, userError ->
                                if (userError != null) {
                                    Log.e("CommentViewModel", "Error fetching user details: ${userError.message}")
                                    return@addSnapshotListener
                                }

                                val firstName = userSnapshot?.getString("firstName") ?: comment.firstName
                                val lastName = userSnapshot?.getString("lastName") ?: comment.lastName
                                val profileImageUrl = userSnapshot?.getString("profileImageUrl") ?: comment.profileImageUrl

                                // Update comment with latest user details
                                comment.firstName = firstName
                                comment.lastName = lastName
                                comment.profileImageUrl = profileImageUrl

                                // Refresh the comments list
                                _comments.value = commentsList
                            }

                        commentsList.add(comment)
                    }
                }
            }
    }

    fun postComment(postId: String, content: String) {
        Log.d(TAG, "Posting a new comment for postId: $postId")
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            val firstName = document.getString("firstName") ?: ""
            val lastName = document.getString("lastName") ?: ""
            val profileImageUrl = document.getString("profileImageUrl") ?: ""

            val newComment = Comment(
                postId = postId,
                userId = currentUser.uid,
                firstName = firstName,
                lastName = lastName,
                profileImageUrl = profileImageUrl,
                content = content
            )

            db.collection("posts").document(postId).collection("comments").add(newComment)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Comment successfully posted with id: ${documentReference.id}")
                    newComment.id = documentReference.id
                    documentReference.set(newComment)
                    incrementCommentCount(postId)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error posting comment: ${e.message}")
                }
        }
    }

    private fun incrementCommentCount(postId: String) {
        val postRef = db.collection("posts").document(postId)
        db.runTransaction { transaction ->
            transaction.update(postRef, "commentCount", FieldValue.increment(1))
        }.addOnSuccessListener {
            Log.d(TAG, "Comment count incremented for postId: $postId")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error incrementing comment count: ${e.message}")
        }
    }
    fun postReply(postId: String, parentCommentId: String, replyContent: String) {
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val profileImageUrl = document.getString("profileImageUrl") ?: ""

                val reply = Comment(
                    postId = postId,
                    userId = currentUser.uid,
                    firstName = firstName,
                    lastName = lastName,
                    profileImageUrl = profileImageUrl,
                    content = replyContent,
                    parentCommentId = parentCommentId // ✅ Set parent comment ID
                )

                db.collection("posts").document(postId)
                    .collection("comments").add(reply)
                    .addOnSuccessListener { documentReference ->
                        Log.d("CommentViewModel", "Reply successfully posted with id: ${documentReference.id}")
                        reply.id = documentReference.id
                        documentReference.set(reply)

                        // ✅ Optionally, increment reply count in parent comment
                        incrementReplyCount(postId, parentCommentId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("CommentViewModel", "Error posting reply: ${e.message}")
                    }
            }
    }

    private fun incrementReplyCount(postId: String, parentCommentId: String) {
        val parentCommentRef = db.collection("posts").document(postId)
            .collection("comments").document(parentCommentId)

        db.runTransaction { transaction ->
            transaction.update(parentCommentRef, "replyCount", FieldValue.increment(1))
        }.addOnSuccessListener {
            Log.d("CommentViewModel", "Reply count incremented for comment: $parentCommentId")
        }.addOnFailureListener { e ->
            Log.e("CommentViewModel", "Error incrementing reply count: ${e.message}")
        }
    }


    fun likeComment(postId: String, commentId: String, liked: Boolean) {
        Log.d(TAG, "Liking/unliking comment with id: $commentId for postId: $postId")
        val currentUser = auth.currentUser ?: return
        val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val currentLikes = snapshot.get("likedBy") as? List<String> ?: listOf()

            if (liked) {
                if (!currentLikes.contains(currentUser.uid)) {
                    transaction.update(commentRef, "likedBy", FieldValue.arrayUnion(currentUser.uid))
                    transaction.update(commentRef, "likeCount", FieldValue.increment(1))
                }
            } else {
                if (currentLikes.contains(currentUser.uid)) {
                    transaction.update(commentRef, "likedBy", FieldValue.arrayRemove(currentUser.uid))
                    transaction.update(commentRef, "likeCount", FieldValue.increment(-1))
                }
            }
        }.addOnSuccessListener {
            Log.d(TAG, "Like/unlike transaction successful for comment: $commentId")
            fetchComments(postId)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error liking/unliking comment: ${e.message}")
        }
    }
}
