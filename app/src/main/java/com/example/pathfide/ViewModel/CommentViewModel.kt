package com.example.pathfide.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfide.Model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CommentViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    companion object {
        const val TAG = "CommentViewModel"
    }

    fun fetchComments(postId: String) {
        Log.d(TAG, "Fetching comments for postId: $postId")

        db.collection("posts").document(postId).collection("comments")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error fetching comments: ${e.message}")
                    return@addSnapshotListener
                }

                val allComments = snapshot?.documents?.mapNotNull { doc ->
                    Log.d(TAG, "Processing comment document with id: ${doc.id}")
                    doc.toObject(Comment::class.java)?.apply {
                        id = doc.id
                        isLiked = likedBy.contains(auth.currentUser?.uid)
                        likeCount = likedBy.size
                        firstName = doc.getString("firstName") ?: ""
                        lastName = doc.getString("lastName") ?: ""
                    }
                } ?: emptyList()

                // Sort by creation time (optional, based on your design)
                val sortedComments = allComments.sortedBy { it.timestamp } // Make sure you have a timestamp field

                _comments.value = sortedComments
                Log.d(TAG, "Comments successfully fetched and organized")
            }
    }

    fun postComment(postId: String, content: String) {
        Log.d(TAG, "Posting a new comment for postId: $postId")
        val currentUser = auth.currentUser ?: return

        val newComment = Comment(
            postId = postId,
            userId = currentUser.uid,
            content = content
        )

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            newComment.firstName = document.getString("firstName") ?: ""
            newComment.lastName = document.getString("lastName") ?: ""
            val profileImageUrl = document.getString("profileImageUrl") ?: ""
            newComment.profileImageUrl = profileImageUrl

            db.collection("posts").document(postId).collection("comments").add(newComment)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Comment successfully posted with id: ${documentReference.id}")
                    newComment.id = documentReference.id
                    documentReference.set(newComment)

                    // Update the comment count in the post
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

    fun likeComment(postId: String, commentId: String, liked: Boolean) {
        Log.d(TAG, "Liking/unliking comment with id: $commentId for postId: $postId")
        val currentUser = auth.currentUser ?: return
        val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val currentLikes = snapshot.get("likedBy") as? List<String> ?: listOf()

            if (currentLikes.contains(currentUser.uid)) {
                // User already liked the comment, remove like
                transaction.update(commentRef, "likedBy", FieldValue.arrayRemove(currentUser.uid))
                Log.d(TAG, "Removed like from comment: $commentId")
                transaction.update(commentRef, "likeCount", FieldValue.increment(-1))
            } else {
                // User hasn't liked the comment, add like
                transaction.update(commentRef, "likedBy", FieldValue.arrayUnion(currentUser.uid))
                Log.d(TAG, "Added like to comment: $commentId")
                transaction.update(commentRef, "likeCount", FieldValue.increment(1))
            }
        }.addOnSuccessListener {
            Log.d(TAG, "Transaction successful for liking/unliking comment: $commentId")
            fetchComments(postId) // Fetch updated comments after the transaction
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error liking/unliking comment: ${e.message}")
        }
    }

    fun postReply(postId: String, parentCommentId: String, content: String) {
        Log.d(TAG, "Posting a reply to commentId: $parentCommentId for postId: $postId")
        val currentUser = auth.currentUser ?: return

        val newReply = Comment(
            postId = postId,
            userId = currentUser.uid,
            content = content,
            parentCommentId = parentCommentId // This ensures it's linked to the parent
        )

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            newReply.firstName = document.getString("firstName") ?: ""
            newReply.lastName = document.getString("lastName") ?: ""
            newReply.profileImageUrl = document.getString("profileImageUrl") ?: ""

            db.collection("posts").document(postId)
                .collection("comments")
                .add(newReply)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Reply successfully posted with id: ${documentReference.id}")
                    newReply.id = documentReference.id
                    documentReference.set(newReply)

                    // Update the comment count in the post
                    incrementCommentCount(postId)
                }
        }
    }

}
