    package com.example.pathfide.ViewModel
    
    import android.util.Log
    import androidx.lifecycle.LiveData
    import androidx.lifecycle.MutableLiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.pathfide.Model.Comment
    import com.example.pathfide.Model.Post
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FieldValue
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.Query
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await
    
    class PostViewModel : ViewModel() {
        private val _posts = MutableLiveData<List<Post>>()
        val posts: LiveData<List<Post>> = _posts
    
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading
    
        private val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        private lateinit var db: FirebaseFirestore

        // Fetch all posts and attach a listener for each post to fetch its comments
        fun fetchPosts() {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val snapshot = firestore.collection("posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .await()
    
                    val fetchedPosts = snapshot.toObjects(Post::class.java)
                    fetchedPosts.forEach { post ->
                        post.likeCount = post.likedBy.size
                        post.isLiked = post.likedBy.contains(auth.currentUser?.uid)
    
                        // Set up a LiveData observer for comments count
                        fetchComments(post.id).observeForever { comments ->
                            post.commentCount = comments.size
                            _posts.postValue(fetchedPosts)  // Notify changes
                        }
                    }
                    _posts.value = fetchedPosts
                } catch (e: Exception) {
                    Log.e("PostViewModel", "Error fetching posts: ${e.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun getCurrentUserId(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid
        }

        fun deletePost(postId: String) {
            viewModelScope.launch {
                try {
                    firestore.collection("posts").document(postId).delete().await()
                    fetchPosts()  // Refresh posts after deletion
                } catch (e: Exception) {
                    Log.e("PostViewModel", "Error deleting post: ${e.message}")
                }
            }
        }

    
        // Fetch comments for a particular post
        fun fetchComments(postId: String): LiveData<List<Comment>> {
            val commentsLiveData = MutableLiveData<List<Comment>>()
    
            firestore.collection("posts").document(postId).collection("comments")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("PostViewModel", "Listen failed.", e)
                        commentsLiveData.value = emptyList()
                        return@addSnapshotListener
                    }
    
                    val commentsList = snapshot?.documents?.mapNotNull { it.toObject(Comment::class.java) } ?: emptyList()
                    commentsLiveData.value = commentsList
                }
    
            return commentsLiveData
        }
    
        // Create a post by the current authenticated user
        fun createPost(content: String) {
            viewModelScope.launch {
                try {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // Fetch the user's profile document from Firestore
                        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                        val firstName = userDoc.getString("firstName") ?: "Anonymous"
                        val lastName = userDoc.getString("lastName") ?: ""
                        val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""  // This retrieves the profile image URL

                        Log.d("PostViewModel", "Creating post with first name: $firstName and last name: $lastName")

                        // Create the post object including the profileImageUrl
                        val post = Post(
                            userId = currentUser.uid,
                            firstName = firstName,
                            lastName = lastName,
                            profileImageUrl = profileImageUrl,  // Attach the profile image URL here
                            content = content
                        )
                        // Add the post to Firestore
                        firestore.collection("posts").add(post).await()

                        // Refresh the post list after creating a post
                        fetchPosts()
                    } else {
                        Log.e("PostViewModel", "User not logged in, cannot create post.")
                    }
                } catch (e: Exception) {
                    Log.e("PostViewModel", "Error creating post: ${e.message}")
                }
            }
        }

        fun fetchPostsRealtime() {
            db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("PostViewModel", "Listen failed: ${e.message}")
                        return@addSnapshotListener
                    }

                    val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                    _posts.value = posts
                }
        }


        // Like or unlike a post
        fun likePost(postId: String, isLiked: Boolean) {
            viewModelScope.launch {
                try {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val postRef = firestore.collection("posts").document(postId)
                        firestore.runTransaction { transaction ->
                            val post = transaction.get(postRef).toObject(Post::class.java)
                            post?.let {
                                if (isLiked) {
                                    if (!it.likedBy.contains(currentUser.uid)) {
                                        it.likedBy.add(currentUser.uid)
                                        it.likeCount++
                                    }
                                } else {
                                    if (it.likedBy.contains(currentUser.uid)) {
                                        it.likedBy.remove(currentUser.uid)
                                        it.likeCount = maxOf(0, it.likeCount - 1)
                                    }
                                }
                                transaction.update(postRef, "likeCount", it.likeCount)
                                transaction.update(postRef, "likedBy", it.likedBy)
                            }
                        }.await()
                        fetchPosts()  // Refresh the post list after liking or unliking
                    }
                } catch (e: Exception) {
                    Log.e("PostViewModel", "Error liking post: ${e.message}")
                }
            }
        }
    
        // Add a comment to a post
        fun addComment(postId: String, content: String) {
            viewModelScope.launch {
                try {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                        val firstName = userDoc.getString("firstName") ?: "Anonymous"
                        val lastName = userDoc.getString("lastName") ?: ""
                        val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""
    
                        val comment = Comment(
                            id = "",  // Firestore will auto-generate the ID
                            postId = postId,
                            userId = currentUser.uid,
                            firstName = firstName,
                            lastName = lastName,
                            profileImageUrl = profileImageUrl,
                            content = content
                        )
    
                        // Add the comment to the comments subcollection
                        firestore.collection("posts").document(postId).collection("comments").add(comment).await()
    
                        // Increment the comment count in the post document
                        val postRef = firestore.collection("posts").document(postId)
                        firestore.runTransaction { transaction ->
                            transaction.update(postRef, "commentCount", FieldValue.increment(1))
                        }.await()
                    }
                } catch (e: Exception) {
                    Log.e("PostViewModel", "Error adding comment: ${e.message}")
                }
            }
        }
        fun getTimeAgo(timestamp: com.google.firebase.Timestamp?): String {
            if (timestamp == null) return ""

            val now = System.currentTimeMillis()
            val postTime = timestamp.toDate().time
            val diff = now - postTime

            return when {
                diff < 60 * 1000 -> "just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
                diff < 48 * 60 * 60 * 1000 -> "yesterday"
                else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            }
        }
    }
