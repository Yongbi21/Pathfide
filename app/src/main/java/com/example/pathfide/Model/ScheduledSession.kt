package com.example.pathfide.Model

import com.google.firebase.Timestamp

data class ScheduledSession(
    val appointmentId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val sessionType: String = "",
    var isAccepted: Boolean? = null,
    val responseTimestamp: Long = 0,
    var id: String? = null,
    val userId: String = "",
    val needsAction: Boolean = false,
    val status: String? = null,
    val therapistId: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val paymentStatus: String = "Pending",
    val isRebooking: Boolean = false,
    val isCompleted: Boolean = false

)
