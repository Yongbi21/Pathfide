            package com.example.pathfide.ViewModel
    
            import android.util.Log
            import androidx.lifecycle.LiveData
            import androidx.lifecycle.MutableLiveData
            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.viewModelScope
            import com.example.pathfide.Adapter.PaymentNotification
            import com.example.pathfide.Fragments.AppointmentResponseStatus
            import com.example.pathfide.Model.ScheduledSession
            import com.google.firebase.auth.FirebaseAuth
            import com.google.firebase.firestore.FirebaseFirestore
            import com.google.firebase.firestore.FirebaseFirestoreException
            import com.google.firebase.firestore.QuerySnapshot
            import kotlinx.coroutines.delay
            import kotlinx.coroutines.launch
            import kotlinx.coroutines.tasks.await
            import android.content.Context
    
    
            class NotificationViewModel : ViewModel() {
                private val _hasNotifications = MutableLiveData<Boolean>()
                val hasNotifications: LiveData<Boolean> get() = _hasNotifications
                private val _scheduledSessions = MutableLiveData<List<ScheduledSession>>()
                val scheduledSessions: LiveData<List<ScheduledSession>> get() = _scheduledSessions
                private val firestore = FirebaseFirestore.getInstance()
                private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                private val _appointmentResponseStatus = MutableLiveData<AppointmentResponseStatus?>()
                val appointmentResponseStatus: MutableLiveData<AppointmentResponseStatus?> =
                    _appointmentResponseStatus
                private val db = FirebaseFirestore.getInstance()
                private val _sessionsWithStatus = MutableLiveData<List<ScheduledSession>>()
                val sessionsWithStatus: LiveData<List<ScheduledSession>> get() = _sessionsWithStatus
                private val _paymentNotifications = MutableLiveData<List<PaymentNotification>>()
                val paymentNotifications: LiveData<List<PaymentNotification>> = _paymentNotifications
                private var userType: String = "CLIENT"
                private lateinit var context: Context
    
    
                init {
                    _hasNotifications.value = false
                }
    
                fun setUserType(type: String) {
                    userType = type
                }
    
    
                fun clearNotifications() {
                    _hasNotifications.value = false
                }

                fun fetchScheduledSessions() {
                    firestore.collection("scheduledSessions")
                        .get()
                        .addOnSuccessListener { result ->
                            val sessions = result.documents.mapNotNull { document ->
                                document.toObject(ScheduledSession::class.java)?.copy(
                                    id = document.id,
                                    firstName = document.getString("firstName") ?: "",
                                    lastName = document.getString("lastName") ?: ""

                                )
                            }
                            _scheduledSessions.value = sessions.reversed()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("NotificationViewModel", "Error fetching scheduled sessions", exception)
                            _scheduledSessions.value = emptyList()
                        }
                }
    
    
                fun observeNotifications(userType: String) {
                    // Regular user notifications
                    val userNotificationsQuery = firestore.collection("notifications")
                        .whereEqualTo("clientId", currentUserId)
    
                    // Therapists get both their direct notifications and session tracking
                    val therapistNotificationsQuery = if (userType == "THERAPIST") {
                        firestore.collection("notifications").whereEqualTo("therapistId", currentUserId)
                    } else null
    
                    val therapistSessionsQuery = if (userType == "THERAPIST") {
                        firestore.collection("scheduledSessions")
                    } else null
    
                    userNotificationsQuery.addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            Log.e("NotificationViewModel", "Direct notifications listen failed.", exception)
                            return@addSnapshotListener
                        }
                        handleNotificationSnapshot(snapshot, "direct")
                    }
    
                    therapistNotificationsQuery?.addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            Log.e("NotificationViewModel", "Therapist notifications listen failed.", exception)
                            return@addSnapshotListener
                        }
                        handleNotificationSnapshot(snapshot, "therapist")
                    }
    
                    therapistSessionsQuery?.addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            Log.e("NotificationViewModel", "Therapist session notifications listen failed.", exception)
                            return@addSnapshotListener
                        }
    
                        if (snapshot != null && !snapshot.isEmpty) {
                            viewModelScope.launch {
                                val sessionIds = snapshot.documents.mapNotNull { it.id }
                                val hasUnrespondedSessions = sessionIds.any { sessionId ->
                                    val response = firestore.collection("appointmentResponse")
                                        .whereEqualTo("appointmentId", sessionId)
                                        .get()
                                        .await()
                                    response.isEmpty
                                }
                                _hasNotifications.value = hasUnrespondedSessions
                            }
                        } else {
                            _hasNotifications.value = false
                        }
                    }
                }
    
                fun markAllNotificationsAsRead() {
                    val collectionRef = firestore.collection("notifications")
    
                    val query = when (userType) {
                        "CLIENT" -> collectionRef.whereEqualTo("clientId", currentUserId).whereEqualTo("isReadClient", false)
                        "THERAPIST" -> collectionRef.whereEqualTo("therapistId", currentUserId).whereEqualTo("isReadTherapist", false)
                        else -> {
                            Log.e("NotificationViewModel", "Unknown user type: $userType")
                            return
                        }
                    }
    
                    query.get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                Log.d("NotificationViewModel", "No unread notifications to mark as read for $userType.")
                                _hasNotifications.postValue(false)
                                return@addOnSuccessListener
                            }
    
                            val batch = firestore.batch()
                            for (document in documents) {
                                val fieldToUpdate = when (userType) {
                                    "CLIENT" -> "isReadClient"
                                    "THERAPIST" -> "isReadTherapist"
                                    else -> {
                                        Log.e("NotificationViewModel", "Invalid user type: $userType")
                                        return@addOnSuccessListener
                                    }
                                }
                                batch.update(document.reference, fieldToUpdate, true)
                            }
    
                            batch.commit()
                                .addOnSuccessListener {
                                    Log.d("NotificationViewModel", "All notifications marked as read for $userType.")
                                    _hasNotifications.postValue(false)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("NotificationViewModel", "Failed to update notifications: $e")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("NotificationViewModel", "Error fetching notifications: $e")
                        }
                }
    
                private fun handleNotificationSnapshot(snapshot: QuerySnapshot?, source: String) {
                    if (snapshot != null && !snapshot.isEmpty) {
                        val hasUnread = snapshot.documents.any { doc ->
                            when (userType) {
                                "CLIENT" -> doc.getBoolean("isReadClient") == false
                                "THERAPIST" -> doc.getBoolean("isReadTherapist") == false
                                else -> false
                            }
                        }
                        _hasNotifications.value = hasUnread
                        Log.d("NotificationViewModel", "Unread notifications found from $source for user $currentUserId")
                    } else {
                        _hasNotifications.value = false
                        Log.d("NotificationViewModel", "No new notifications from $source for user $currentUserId")
                    }
                }
    
                fun observePaymentNotifications() {
                    // Determine the correct Firestore collection and query based on user type
                    val collectionRef = firestore.collection("payments")
                    val query = when (userType) {
                        "CLIENT" -> collectionRef.whereEqualTo("clientId", currentUserId)
                        "THERAPIST" -> collectionRef.whereEqualTo("therapistId", currentUserId)
                        else -> {
                            Log.e("NotificationViewModel", "Invalid user type: $userType")
                            return // Exit if user type is invalid
                        }
                    }
    
                    query.addSnapshotListener { snapshot, exception ->
                        handlePaymentSnapshot(snapshot, exception)
                    }
                }
    
                private fun handlePaymentSnapshot(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                    if (exception != null) {
                        Log.e("NotificationViewModel", "Payment notifications listen failed.", exception)
                        return
                    }
    
                    Log.d("NotificationViewModel", "Snapshot: ${snapshot?.documents?.size} payments found")
    
                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        val status = doc.getString("status") ?: "Unknown"
                        val message = when (userType) {
                            "CLIENT" -> getClientPaymentMessage(status)
                            "THERAPIST" -> getTherapistPaymentMessage(status)
                            else -> "Payment Status: $status"
                        }
    
                        PaymentNotification(
                            id = doc.id,
                            title = "Payment Notification",
                            message = message,
                            status = status,
                            paymentId = doc.id,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } ?: emptyList()
    
                    if (notifications.isNotEmpty()) {
                        Log.d("NotificationViewModel", "Notifications: ${notifications.size}")
                    } else {
                        Log.d("NotificationViewModel", "No payment notifications found")
                    }
    
                    _paymentNotifications.value = notifications
    
                    // Update _hasNotifications based on whether there are any unread payment notifications
                    val hasUnreadPaymentNotifications = notifications.any { !it.isRead }
                    _hasNotifications.postValue(hasUnreadPaymentNotifications)
                }
    
                private fun getClientPaymentMessage(status: String): String {
                    return when (status) {
                        "Payment Successful!" -> "Your payment has been confirmed. Your appointment is now scheduled."
                        "Payment Declined" -> "Your payment was declined. Please check your payment details and try again."
                        else -> "Your payment is being processed. Please wait for confirmation."
                    }
                }
    
                private fun getTherapistPaymentMessage(status: String): String {
                    return when (status) {
                        "Payment Successful!" -> "A client's payment has been confirmed. The appointment is now scheduled."
                        "Payment Declined" -> "A client's payment was declined. The appointment is not confirmed."
                        else -> "A client's payment is being processed. Awaiting confirmation."
                    }
                }
    
                fun acceptAppointment(appointmentId: String) {
                    viewModelScope.launch {
                        try {
                            _appointmentResponseStatus.value = AppointmentResponseStatus.Loading
                            addResponseToFirestore(appointmentId, isAccepted = true)
                            markNotificationAsRead(appointmentId) // Mark notification as read
                            _appointmentResponseStatus.value = AppointmentResponseStatus.Success(true)
                            fetchScheduledSessions()
                            clearResponseStatus()
                        } catch (e: Exception) {
                            _appointmentResponseStatus.value = AppointmentResponseStatus.Error(e.message ?: "Unknown error occurred")
                        }
                    }
                }
    
                fun rejectAppointment(appointmentId: String) {
                    viewModelScope.launch {
                        try {
                            _appointmentResponseStatus.value = AppointmentResponseStatus.Loading
                            addResponseToFirestore(appointmentId, isAccepted = false)
                            markNotificationAsRead(appointmentId) // Mark notification as read
                            _appointmentResponseStatus.value = AppointmentResponseStatus.Success(false)
                            fetchScheduledSessions()
                            clearResponseStatus()
                        } catch (e: Exception) {
                            _appointmentResponseStatus.value = AppointmentResponseStatus.Error(e.message ?: "Unknown error occurred")
                        }
                    }
                }
    
                private suspend fun markNotificationAsRead(appointmentId: String) {
                    try {
                        val fieldToUpdate = when (userType) {
                            "CLIENT" -> "isReadClient"
                            "THERAPIST" -> "isReadTherapist"
                            else -> return
                        }
    
                        val query = when (userType) {
                            "CLIENT" -> firestore.collection("notifications")
                                .whereEqualTo("appointmentId", appointmentId)
                                .whereEqualTo("clientId", currentUserId)
                            "THERAPIST" -> firestore.collection("notifications")
                                .whereEqualTo("appointmentId", appointmentId)
                                .whereEqualTo("therapistId", currentUserId)
                            else -> return
                        }
    
                        val notificationQuery = query.get().await()
    
                        if (notificationQuery.documents.isNotEmpty()) {
                            val notificationDoc = notificationQuery.documents.firstOrNull()
    
                            firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(notificationDoc?.reference!!)
                                transaction.update(snapshot.reference, fieldToUpdate, true)
                            }.addOnSuccessListener {
                                Log.d("NotificationViewModel", "Notification marked as read for $userType, appointmentId: $appointmentId")
                                fetchScheduledSessions()
                            }.addOnFailureListener { e ->
                                Log.e("NotificationViewModel", "Error marking notification as read", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NotificationViewModel", "Error marking notification as read", e)
                    }
                }
    
    
                private suspend fun fetchUserName(userId: String): Pair<String?, String?> {
                    return try {
                        val userDocument = firestore.collection("users").document(userId).get().await()
                        val firstName = userDocument.getString("firstName")
                        val lastName = userDocument.getString("lastName")
                        Pair(firstName, lastName)
                    } catch (e: Exception) {
                        Log.e("NotificationViewModel", "Error fetching user details", e)
                        Pair(null, null)
                    }
                }
    
                // Helper function to add response to Firestore
                private suspend fun addResponseToFirestore(appointmentId: String, isAccepted: Boolean) {
                    val (firstName, lastName) = fetchUserName(currentUserId) // Fetch user name
                    Log.d("NotificationViewModel", "Adding response for appointmentId: $appointmentId")
    
                    val responseData = hashMapOf(
                        "appointmentId" to appointmentId,
                        "therapistId" to currentUserId,
                        "isAccepted" to isAccepted,
                        "responseTimestamp" to System.currentTimeMillis(),
                        "firstName" to firstName, // Include first name
                        "lastName" to lastName   // Include last name
    
    
                    )
    
                    // Create a new document with auto-generated ID
                    firestore.collection("appointmentResponse")
                        .add(responseData)
                        .addOnSuccessListener { documentRef ->
                            Log.d(
                                "NotificationViewModel",
                                "Response added to Firestore for appointment: $appointmentId with document ID: ${documentRef.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "NotificationViewModel",
                                "Failed to add response to Firestore for appointmentId: $appointmentId",
                                e
                            )
                        }
                }
    
                private fun clearResponseStatus() {
                    viewModelScope.launch {
                        delay(2000)
                        _appointmentResponseStatus.value = null
                    }
                }
    
                fun checkAppointmentStatus(appointmentId: String, callback: (Boolean?) -> Unit) {
                    if (appointmentId.isBlank()) {
                        Log.e("NotificationViewModel", "Invalid appointment ID: $appointmentId")
                        callback(null)
                        return
                    }
    
                    db.collection("appointmentResponse")
                        .whereEqualTo("appointmentId", appointmentId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val document = querySnapshot.documents.firstOrNull()
                                val isAccepted = document?.getBoolean("isAccepted")
                                Log.d(
                                    "NotificationViewModel",
                                    "Found appointment status - isAccepted: $isAccepted"
                                )
                                callback(isAccepted)
                            } else {
                                Log.d(
                                    "NotificationViewModel",
                                    "No response found for appointment: $appointmentId"
                                )
                                callback(null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("NotificationViewModel", "Error checking appointment status", exception)
                            callback(null)
                        }
                }
    
                fun fetchSessionsWithStatus(userType: String) {
                    viewModelScope.launch {
                        Log.d("NotificationViewModel", "Fetching sessions for user type: $userType")
    
                        val query = when (userType) {
                            "CLIENT" -> firestore.collection("scheduledSessions").whereEqualTo("clientId", currentUserId)
                            "THERAPIST" -> firestore.collection("scheduledSessions").whereEqualTo("therapistId", currentUserId)
                            else -> null
                        }
    
                        if (query == null) {
                            Log.e("NotificationViewModel", "Invalid userType provided: $userType")
                            _sessionsWithStatus.value = emptyList()
                            return@launch
                        }
    
                        try {
                            val result = query.get().await()
                            Log.d("NotificationViewModel", "Fetched sessions for $userType - Document count: ${result.size()}")
    
                            val sessions = result.documents.mapNotNull { doc ->
                                doc.toObject(ScheduledSession::class.java)?.copy(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    firstName = doc.getString("firstName") ?: "",
                                    lastName = doc.getString("lastName") ?: ""
                                )
                            }
    
                            // Fetch correct payment status for each session
                            val updatedSessions = sessions.map { session ->
                                // Change the query to match on therapistId and clientId
                                // Since we can't be sure which side of the relationship we're on
                                val paymentQuery = when (userType) {
                                    "CLIENT" -> firestore.collection("payments")
                                        .whereEqualTo("clientId", currentUserId)
                                        .whereEqualTo("therapistId", session.therapistId)
                                    "THERAPIST" -> firestore.collection("payments")
                                        .whereEqualTo("therapistId", currentUserId)
                                        .whereEqualTo("clientId", session.userId)
                                    else -> null
                                }
    
                                val paymentStatus = if (paymentQuery != null) {
                                    val paymentDocs = paymentQuery.get().await()
                                    val paymentDoc = paymentDocs.documents.firstOrNull()
                                    val status = paymentDoc?.getString("status") ?: "Pending"
    
                                    Log.d("NotificationViewModel", "Session ${session.id} - Payment ID: ${paymentDoc?.id}, Status: $status")
    
                                    when (status) {
                                        "Payment Successful!" -> "Paid"
                                        "Paid" -> "Paid"
                                        "Payment Declined" -> "Declined"
                                        "Declined" -> "Declined"
                                        else -> "Pending"
                                    }
                                } else {
                                    "Pending"
                                }
    
                                session.copy(paymentStatus = paymentStatus)
                            }
    
                            if (updatedSessions.isNotEmpty()) {
                                addAcceptanceStatus(updatedSessions, userType)
                            } else {
                                _hasNotifications.value = false
                                _sessionsWithStatus.value = emptyList()
                                Log.d("NotificationViewModel", "No sessions found for userType: $userType")
                            }
    
                        } catch (exception: Exception) {
                            Log.e("NotificationViewModel", "Error fetching sessions", exception)
                            _hasNotifications.value = false
                            _sessionsWithStatus.value = emptyList()
                        }
                    }
                }
    
                private suspend fun addAcceptanceStatus(sessions: List<ScheduledSession>, userType: String) {
                    val updatedSessions = mutableListOf<ScheduledSession>()
                    val responses = mutableMapOf<String, Boolean?>()  // Map to store responses by appointmentId
    
                    // Fetch all responses once for processing
                    try {
                        val responseSnapshot = firestore.collection("appointmentResponse")
                            .whereIn("appointmentId", sessions.map { it.id ?: "" })
                            .get()
                            .await()
    
                        // Populate the responses map
                        responseSnapshot.documents.forEach { doc ->
                            val appointmentId = doc.getString("appointmentId")
                            val isAccepted = doc.getBoolean("isAccepted")
                            if (appointmentId != null) {
                                responses[appointmentId] = isAccepted
                            }
                        }
                        Log.d("NotificationViewModel", "Responses map: $responses")
                    } catch (e: Exception) {
                        Log.e("NotificationViewModel", "Error fetching responses", e)
                    }
    
                    // Process sessions and add the acceptance status
                    for (session in sessions) {
                        try {
                            val isAccepted = responses[session.id]  // This fetches the response status
                            val updatedSession = session.copy(isAccepted = isAccepted)
                            updatedSessions.add(updatedSession)
                        } catch (e: Exception) {
                            Log.e("NotificationViewModel", "Error processing session: ${session.id}", e)
                        }
                    }
    
                    // Sort and update LiveData
                    val sortedSessions = updatedSessions.sortedByDescending {
                        it.appointmentDate + it.appointmentTime
                    }
                    _sessionsWithStatus.value = sortedSessions
                    _hasNotifications.value = sortedSessions.isNotEmpty()
    
                    Log.d("NotificationViewModel", "Processed and updated sessions with status for userType: $userType")
                }
    
    
    
            }
    
    
