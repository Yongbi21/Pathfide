package com.example.pathfide.Utils


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

object PaymentNotificationHelper {
    private val db = FirebaseFirestore.getInstance()

    fun sendPaymentStatusNotification(
        clientId: String,
        therapistId: String,
        status: String,
        paymentId: String
    ) {
        // Create notification for user
        createNotification(
            recipientId = clientId,
            title = "Payment Status Update",
            message = "Your payment has been $status",
            type = "PAYMENT",
            paymentId = paymentId,
            isRead = false
        )

        // Create notification for therapist
        createNotification(
            recipientId = therapistId,
            title = "Payment Status Update",
            message = "A payment has been $status",
            type = "PAYMENT",
            paymentId = paymentId,
            isRead = false
        )
    }

    private fun createNotification(
        recipientId: String,
        title: String,
        message: String,
        type: String,
        paymentId: String,
        isRead: Boolean
    ) {
        val notification = hashMapOf(
            "recipientId" to recipientId,
            "title" to title,
            "message" to message,
            "type" to type,
            "paymentId" to paymentId,
            "timestamp" to Date(),
            "isRead" to isRead
        )

        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener { documentReference ->
                Log.d("PaymentNotification", "Notification sent successfully to $recipientId")
            }
            .addOnFailureListener { e ->
                Log.e("PaymentNotification", "Error sending notification to $recipientId", e)
            }
    }
}