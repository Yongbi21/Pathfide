package com.example.pathfide.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfide.Model.Chat
import com.example.pathfide.Model.Message
import com.example.pathfide.Model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val messagesLiveData = MutableLiveData<List<Message>>()
    private val _recentChats = MutableLiveData<List<Chat>>()
    val recentChats: LiveData<List<Chat>> = _recentChats
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _userProfile = MutableLiveData<User>()
    val userProfile: LiveData<User> = _userProfile

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Fetch messages from Firestore
    fun getMessages(chatId: String): LiveData<List<Message>> {
        if (chatId.isNotEmpty()) {
            firestore.collection("Chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("ChatViewModel", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val messages = snapshot.toObjects(Message::class.java)
                        messagesLiveData.value = messages
                    } else {
                        messagesLiveData.value = emptyList()
                    }
                }
        } else {
            messagesLiveData.value = emptyList()
        }

        return messagesLiveData // Return the same LiveData instance
    }

    // Send a message to Firestore
    fun sendMessage(chatId: String, message: Message) {
        val messageData = hashMapOf(
            "senderId" to message.senderId,
            "receiverId" to message.receiverId,
            "content" to message.content,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("Chats").document(chatId)
            .collection("messages").add(messageData)
            .addOnSuccessListener {
                // Message sent successfully
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error sending message: ${e.message}"
                Log.e("ChatViewModel", "Error sending message", e)
            }

    }



    fun fetchRecentChats(currentUserId: String) {
        val recentChatsMap = mutableMapOf<String, Chat>()
        var pendingSenderFetches = 0
        var pendingReceiverFetches = 0
        var senderFetchComplete = false
        var receiverFetchComplete = false

        // Fetch messages where user is sender
        firestore.collectionGroup("messages")
            .whereEqualTo("senderId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                pendingSenderFetches = documents.size()

                if (documents.isEmpty) {
                    senderFetchComplete = true
                    checkAndUpdateList(recentChatsMap, senderFetchComplete, receiverFetchComplete)
                }

                documents.forEach { document ->
                    val chatId = document.reference.parent.parent?.id
                    val lastMessage = document.toObject(Message::class.java)

                    if (chatId != null) {
                        firestore.collection("Users").document(lastMessage.receiverId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val chat = Chat(
                                    id = chatId,
                                    lastMessage = lastMessage.content,
                                    lastMessageTimestamp = lastMessage.timestamp,
                                    firstName = userDoc.getString("firstName") ?: "",
                                    lastName = userDoc.getString("lastName") ?: "",
                                    avatarUrl = userDoc.getString("profileImage") ?: "",
                                    userId = lastMessage.receiverId
                                )
                                recentChatsMap[chatId] = chat
                                pendingSenderFetches--

                                if (pendingSenderFetches == 0) {
                                    senderFetchComplete = true
                                    checkAndUpdateList(recentChatsMap, senderFetchComplete, receiverFetchComplete)
                                }
                            }
                    }
                }
            }

        // Fetch messages where user is receiver
        firestore.collectionGroup("messages")
            .whereEqualTo("receiverId", currentUserId)
            .get()
            .addOnSuccessListener { documentsReceiver ->
                pendingReceiverFetches = documentsReceiver.size()

                if (documentsReceiver.isEmpty) {
                    receiverFetchComplete = true
                    checkAndUpdateList(recentChatsMap, senderFetchComplete, receiverFetchComplete)
                }

                documentsReceiver.forEach { document ->
                    val chatId = document.reference.parent.parent?.id
                    val lastMessage = document.toObject(Message::class.java)

                    if (chatId != null) {
                        firestore.collection("Users").document(lastMessage.senderId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val chat = Chat(
                                    id = chatId,
                                    lastMessage = lastMessage.content,
                                    lastMessageTimestamp = lastMessage.timestamp,
                                    firstName = userDoc.getString("firstName") ?: "",
                                    lastName = userDoc.getString("lastName") ?: "",
                                    avatarUrl = userDoc.getString("profileImage") ?: "",
                                    userId = lastMessage.senderId
                                )
                                recentChatsMap[chatId] = chat
                                pendingReceiverFetches--

                                if (pendingReceiverFetches == 0) {
                                    receiverFetchComplete = true
                                    checkAndUpdateList(recentChatsMap, senderFetchComplete, receiverFetchComplete)
                                }
                            }
                    }
                }
            }
    }

    private fun checkAndUpdateList(
        recentChatsMap: MutableMap<String, Chat>,
        senderComplete: Boolean,
        receiverComplete: Boolean
    ) {
        if (senderComplete && receiverComplete) {
            // Both fetches are complete, update the list once
            val sortedList = recentChatsMap.values.sortedByDescending { it.lastMessageTimestamp }
            _recentChats.postValue(sortedList)
        }
    }

    fun fetchLastMessages(chatId: String) {
        // Ensure that chatId is valid and not empty
        if (chatId.isNotEmpty()) {
            firestore.collection("Chats").document(chatId) // This line is critical
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("ChatViewModel", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.documents.isNotEmpty()) {
                        // Handle the last message fetched
                        val lastMessage = snapshot.documents[0]
                        // Process the lastMessage as needed
                    } else {
                        Log.d("ChatViewModel", "No messages found for chatId: $chatId")
                    }
                }
        } else {
            Log.e("ChatViewModel", "Invalid chatId: $chatId")
        }
    }

    // Function to create a new chat
    fun createChat(userId1: String, userId2: String) {
        val chatData = hashMapOf(
            "participants" to listOf(userId1, userId2) // Add user IDs to participants
            // You can add any other initial fields here if needed
        )

        firestore.collection("Chats").add(chatData)
            .addOnSuccessListener { documentReference ->
                Log.d("ChatCreation", "Chat created with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("ChatCreation", "Error creating chat", e)
            }
    }

    // Fetch user profile (name, image) by user ID
    fun getUserProfile(userId: String) {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    _userProfile.value = user
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error fetching user profile", e)
            }
    }

    // Fetch user profile and call callback
    private fun fetchUserProfile(userId: String, callback: (User?) -> Unit) {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null) // User not found
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error fetching user profile", e)
                callback(null)
            }
    }

    // Search for users by name query
    fun searchUsers(query: String) {
        firestore.collection("Users")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ChatViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val users = snapshot.toObjects(User::class.java)
                    _users.value = users
                } else {
                    _users.value = emptyList()
                }
            }
    }

    // Update user status
    fun updateUserStatus(userId: String, isOnline: Boolean) {
        val userRef = firestore.collection("users").document(userId)
        userRef.update("isOnline", isOnline)
            .addOnSuccessListener { Log.d("User Status", "User status updated to $isOnline") }
            .addOnFailureListener { e -> Log.e("User Status", "Error updating user status", e) }
    }

}
