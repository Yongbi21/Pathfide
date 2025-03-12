package com.example.pathfide.Fragments.Chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pathfide.Adapter.ChatAdapter
import com.example.pathfide.Model.Message
import com.example.pathfide.Model.User
import com.example.pathfide.R
import com.example.pathfide.ViewModel.ChatViewModel
import com.example.pathfide.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var statusIndicator: ImageView
    private lateinit var partnerId: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var chatId: String
    private lateinit var currentUserId: String
    private var statusListener: ListenerRegistration? = null
    private var isPartnerOnline = false
    private val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private var partnerFirstName: String = ""
    private var partnerLastName: String = ""
    private var currentUserFirstName: String = ""
    private var currentUserLastName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Firestore and Auth
        firestore = Firebase.firestore
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        statusIndicator = binding.statusIndicator
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        // Setup RecyclerView for messages
        setupRecyclerView()

        // Get the arguments passed from ChatMainFragment
        val args = ChatFragmentArgs.fromBundle(requireArguments())
        val chatPartnerId = args.chatId

        // Extract the correct chat partner ID
        partnerId = if (currentUserId == chatPartnerId.split("|")[0]) {
            chatPartnerId.split("|")[1]
        } else {
            chatPartnerId.split("|")[0]
        }

        // Register call end event listener
        setupCallEndListener()

        // Setup UI and listeners
        setupUIAndListeners()
        fetchBothUsersDetails()
        setupCallInvitation()

        // Initialize chat if IDs are valid
        initializeChat()
    }

    override fun onStart() {
        super.onStart()

        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(
                    "ChatFragment",
                    "User session refreshed: ${FirebaseAuth.getInstance().currentUser?.uid}"
                )
                Handler(Looper.getMainLooper()).postDelayed({
                }, 1000)
            } else {
                Log.e("ChatFragment", "Failed to refresh user session")
            }
        }
    }

    private fun setupCallEndListener() {
        Log.d("ZegoInvite", "⚡ Setting up CallEndListener...") // Log before setting

        ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener =
            object : CallEndListener {
                override fun onCallEnd(reason: ZegoCallEndReason, jsonObject: String?) {
                    Log.d("ZegoInvite", "✅ Call ended with reason: $reason")

                    firestore.collection("Calls").document(chatId).get()
                        .addOnSuccessListener { document ->
                            val startTime = document.getTimestamp("startTime")?.toDate()?.time ?: 0
                            val endTime = System.currentTimeMillis()

                            if (startTime > 0) {
                                val durationInSeconds = (endTime - startTime) / 1000
                                val formattedDuration = formatDuration(durationInSeconds)

                                sendCallEndedMessage("Video call ended. Duration: $formattedDuration")
                            } else {
                                sendCallEndedMessage("The video call ended.")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatFragment", "❌ Failed to fetch call start time: ${e.message}")
                            sendCallEndedMessage("The video call ended.") // Send fallback message
                        }
                }
            }

        Log.d("ZegoInvite", "🔥 CallEndListener has been set")
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, secs)
            minutes > 0 -> String.format("%02d:%02d", minutes, secs)
            else -> String.format("%d sec", secs)
        }
    }



    private fun sendCallEndedMessage(messageText: String = "Video call ended.") {
        firestore.collection("Calls").document(chatId).get()
            .addOnSuccessListener { document ->
                val initiatorId = document.getString("initiatorId") ?: currentUserId

                val messageData = hashMapOf(
                    "content" to messageText,
                    "senderId" to initiatorId,
                    "receiverId" to if (initiatorId == currentUserId) partnerId else currentUserId,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "type" to "system"
                )

                firestore.collection("Chats").document(chatId).collection("messages")
                    .add(messageData)
                    .addOnSuccessListener {
                        Log.d("ChatFragment", "✅ Call-ended system message sent by $initiatorId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatFragment", "❌ Failed to send call-ended message: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragment", "❌ Failed to fetch call initiator: ${e.message}")
            }
    }




    private fun setupRecyclerView() {
        messagesRecyclerView = binding.messagesRecyclerView
        chatAdapter = ChatAdapter(currentUserId)
        messagesRecyclerView.adapter = chatAdapter

        val layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        messagesRecyclerView.layoutManager = layoutManager
    }

    private fun setupUIAndListeners() {
        binding.textSendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun initializeChat() {
        Log.d("ChatFragment", "Chat Partner ID received: $partnerId")
        if (partnerId.isEmpty() || currentUserId.isEmpty()) {
            showError("Invalid user ID detected. Please try again.")
            Log.e(
                "ChatFragment",
                "Invalid user ID detected: currentUserId=$currentUserId, partnerId=$partnerId"
            )
            return
        }
        // Continue with existing logic if IDs are valid
        chatId = generateChatId(currentUserId, partnerId)
        fetchUserDetails(partnerId)
        observeMessages(chatId)
    }

    private fun sendMessage() {
        val messageContent = binding.messageInput.text.toString()
        if (messageContent.isNotEmpty()) {
            val message = Message(
                content = messageContent,
                senderId = currentUserId,
                receiverId = partnerId,
                formattedTime = dateFormat.format(Date())
            )

            chatViewModel.sendMessage(chatId, message)

            // Fetch receiver's FCM token and send notification
            firestore.collection("users").document(partnerId).get()
                .addOnSuccessListener { document ->
                    val fcmToken = document.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to fetch receiver FCM token: ${e.message}")
                }

            binding.messageInput.text.clear()
        }
    }



    private fun fetchUserDetails(userId: String?) {
        if (userId.isNullOrEmpty()) {
            showError("User ID is null or empty. Please try again.")
            return
        }

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        partnerFirstName = it.firstName
                        partnerLastName = it.lastName
                        updateUIWithUserDetails(it)
                        isPartnerOnline = it.isOnline
                        listenForUserStatus(userId)
                    }
                } else {
                    Log.e("ChatFragment", "No such document for user ID: $userId")
                    showError("User not found. Please check the ID.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ChatFragment", "Error fetching user details", exception)
                showError("Error fetching user details. Please try again.")
            }
        // Fetch current user details
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        currentUserFirstName = it.firstName
                        currentUserLastName = it.lastName
                    }
                }
            }
    }




    private fun updateUIWithUserDetails(user: User) {
        binding.apply {
            chatUserName.text = "${user.firstName} ${user.lastName}"
            chatUserStatus.text = if (user.isOnline) "Online" else "Offline"

            Glide.with(this@ChatFragment)
                .load(user.profileImageUrl) // Use profileImageUrl for the image
                .placeholder(R.drawable.person) // Placeholder image while loading
                .error(R.drawable.person) // Fallback for load errors
                .into(chatImageViewUser) // Target ImageView

            updateUserStatus(user.isOnline)
        }
    }

    private fun listenForUserStatus(userId: String) {
        statusListener?.remove()

        statusListener = firestore.collection("users").document(userId)
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    Log.e("ChatFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                documentSnapshot?.let { document ->
                    if (document.exists()) {
                        val isOnline = document.getBoolean("isOnline") ?: false
                        Log.d("ChatFragment", "User status update received: isOnline = $isOnline")
                        isPartnerOnline = isOnline

                        activity?.runOnUiThread {
                            binding.chatUserStatus.text = if (isOnline) "Online" else "Offline"
                            updateUserStatus(isOnline)
                        }
                    }
                }
            }
    }

    private fun observeMessages(chatId: String) {
        chatViewModel.getMessages(chatId).observe(viewLifecycleOwner) { messages ->
            chatAdapter.setMessages(messages)
            messagesRecyclerView.scrollToPosition(messages.size - 1)
        }
    }

    private fun fetchBothUsersDetails() {
        // First fetch partner details
        firestore.collection("users").document(partnerId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        partnerFirstName = it.firstName
                        partnerLastName = it.lastName
                        updateUIWithUserDetails(it)
                        isPartnerOnline = it.isOnline
                        listenForUserStatus(partnerId)

                        // After getting partner details, fetch current user details
                        fetchCurrentUserDetails()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ChatFragment", "Error fetching partner details", exception)
            }
    }

    private fun fetchCurrentUserDetails() {
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        currentUserFirstName = it.firstName
                        currentUserLastName = it.lastName

                        // Now that we have both users' details, setup the call invitation
                        setupCallInvitation()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ChatFragment", "Error fetching current user details", exception)
            }
    }

    private fun setupCallInvitation() {
        val videoCallButton = view?.findViewById<ZegoSendCallInvitationButton>(R.id.voiceButton)

        firestore.collection("users").document(partnerId).get()
            .addOnSuccessListener { partnerDoc ->
                val partnerFirstName = partnerDoc.getString("firstName") ?: ""
                val partnerLastName = partnerDoc.getString("lastName") ?: ""
                val partnerDisplayName = "$partnerFirstName $partnerLastName".trim()

                firestore.collection("users").document(currentUserId).get()
                    .addOnSuccessListener { currentUserDoc ->
                        val currentUserFirstName = currentUserDoc.getString("firstName") ?: ""
                        val currentUserLastName = currentUserDoc.getString("lastName") ?: ""
                        val currentUserDisplayName = "$currentUserFirstName $currentUserLastName".trim()

                        val partnerUser = ZegoUIKitUser(partnerId, partnerDisplayName)

                        videoCallButton?.apply {
                            setInvitees(listOf(partnerUser))
                            setResourceID("zego_call")
                            setIsVideoCall(true)

                            videoCallButton?.setOnClickListener(View.OnClickListener {
                                val callData = hashMapOf(
                                    "initiatorId" to currentUserId,
                                    "receiverId" to partnerId,
                                    "timestamp" to FieldValue.serverTimestamp()
                                )

                                firestore.collection("Calls").document(chatId).set(callData)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "✅ Call document created successfully for chatId: $chatId")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "❌ Error creating call document: ${e.message}")
                                    }
                            })
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "❌ Failed to fetch current user details: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Failed to fetch partner user details: ${e.message}")
            }
    }


    private fun generateChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "$userId1|$userId2"
        } else {
            "$userId2|$userId1"
        }
    }

    private fun updateUserStatus(isOnline: Boolean) {
        val resourceId = if (isOnline) R.drawable.onlinestatus else R.drawable.offlinestatus
        statusIndicator.setImageResource(resourceId)
    }

    private fun showError(message: String) {
        view?.post {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        statusListener?.remove()

        // Clean up call end listener to prevent memory leaks
        ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener = null
    }
}