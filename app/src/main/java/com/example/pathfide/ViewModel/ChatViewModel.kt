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

        return messagesLiveData
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

    // Fetch recent chats and ensure the latest message appears
    fun fetchRecentChats(currentUserId: String) {
        Log.d("ChatViewModel", "Fetching recent chats for user: $currentUserId")

        val recentChatsMap = mutableMapOf<String, Chat>()
        var pendingFetches = 0
        var fetchComplete = false

        // Fetch messages where user is either the sender or receiver
        firestore.collectionGroup("messages")
            .whereIn("senderId", listOf(currentUserId))  // Fetch sent messages
            .get()
            .addOnSuccessListener { sentMessages ->

                firestore.collectionGroup("messages")
                    .whereIn("receiverId", listOf(currentUserId)) // Fetch received messages
                    .get()
                    .addOnSuccessListener { receivedMessages ->

                        val allMessages = sentMessages.documents + receivedMessages.documents
                        pendingFetches = allMessages.size

                        if (allMessages.isEmpty()) {
                            fetchComplete = true
                            checkAndUpdateList(recentChatsMap, fetchComplete)
                        }

                        allMessages.forEach { document ->
                            val chatId = document.reference.parent.parent?.id ?: document.id
                            val lastMessage = document.toObject(Message::class.java) ?: return@forEach

                            val otherUserId = if (lastMessage.senderId == currentUserId) lastMessage.receiverId else lastMessage.senderId

                            firestore.collection("users").document(otherUserId)
                                .get()
                                .addOnSuccessListener { userDoc ->
                                    val chat = Chat(
                                        id = chatId,
                                        lastMessage = lastMessage.content,
                                        lastMessageTimestamp = lastMessage.timestamp,
                                        firstName = userDoc.getString("firstName") ?: "",
                                        lastName = userDoc.getString("lastName") ?: "",
                                        avatarUrl = userDoc.getString("profileImageUrl") ?: "",
                                        userId = otherUserId
                                    )

                                    // Store only the latest message per chat
                                    val existingChat = recentChatsMap[chatId]
                                    if (existingChat == null ||
                                        (lastMessage.timestamp != null &&
                                                existingChat.lastMessageTimestamp != null &&
                                                lastMessage.timestamp > existingChat.lastMessageTimestamp)) {
                                        recentChatsMap[chatId] = chat
                                    }

                                    pendingFetches--
                                    if (pendingFetches == 0) {
                                        fetchComplete = true
                                        checkAndUpdateList(recentChatsMap, fetchComplete)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ChatViewModel", "Error fetching user profile: ${e.message}")
                                    pendingFetches--
                                    if (pendingFetches == 0) {
                                        fetchComplete = true
                                        checkAndUpdateList(recentChatsMap, fetchComplete)
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatViewModel", "Error fetching received messages: ${e.message}")
                        fetchComplete = true
                        checkAndUpdateList(recentChatsMap, fetchComplete)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error fetching sent messages: ${e.message}")
                fetchComplete = true
                checkAndUpdateList(recentChatsMap, fetchComplete)
            }
    }

    private fun checkAndUpdateList(
        recentChatsMap: MutableMap<String, Chat>,
        fetchComplete: Boolean
    ) {
        if (fetchComplete) {
            val sortedList = recentChatsMap.values.sortedByDescending { it.lastMessageTimestamp }
            _recentChats.postValue(sortedList)
        }
    }
}
