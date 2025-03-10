package com.example.pathfide.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.R
import com.google.firebase.Timestamp

data class PaymentNotification(
    val id: String,
    val title: String,
    val message: String,
    val status: String,
    val paymentId: String,
    val isRead: Boolean,
    val timestamp: Timestamp = Timestamp.now(),

)

class PaymentNotificationAdapter(
    private var notifications: List<PaymentNotification>,
    private val userType: String
) : RecyclerView.Adapter<PaymentNotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.notificationIcon)
        val title: TextView = view.findViewById(R.id.notificationTitle)
        val message: TextView = view.findViewById(R.id.notificationMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        Log.d("PaymentNotification", "Notification Status: ${notification.status}")

        val customizedMessage = when (userType) {
            "CLIENT" -> getClientMessage(notification.status)
            "THERAPIST" -> getTherapistMessage(notification.status)
            else -> notification.message
        }



        holder.title.text = notification.title
        holder.message.text = customizedMessage



        // Set icon based on payment status
        val iconResource = when(notification.status) {
            "Payment Successful!" -> R.drawable.check
            "Payment Declined" -> R.drawable.decline
            else -> R.drawable.warning // Default icon if no status matches
        }
        holder.icon.setImageResource(iconResource)
    }

    private fun getClientMessage(status: String): String {
        return when (status) {
            "Payment Successful!" -> {
                "Your appointment has been confirmed. Please check the scheduled date and time in the app to ensure you're prepared. If you need further assistance, visit the appointment section or contact support."
            }
            "Payment Declined" -> {
                "Your payment could not be processed. This may be due to insufficient funds, an expired card, or a network issue. Please verify your payment details and try again to complete your appointment booking. If the issue persists, contact your bank or support for assistance."
            }
            else -> "Your payment is currently being processed. This may take a few minutes and sometimes an hour. Please do not refresh or retry the payment immediately. You can check your gcash if there is no deduction upon paying. If the payment is not confirmed within an hour, contact support for further assistance."
        }
    }

    private fun getTherapistMessage(status: String): String {
        return when (status) {
            "Payment Successful!" -> {
                "A client's payment has been confirmed. The appointment has been added to your schedule. Please check the appointment details in your appointment history."
            }
            "Payment Declined" -> {
                "A client's payment was declined. The appointment is not confirmed. The client has been notified to try again with valid payment details."
            }
            else -> "A client's payment is being processed. You will be notified once the payment is confirmed or if any issues arise."
        }
    }


    override fun getItemCount(): Int {
        return notifications.size
    }

    fun updateNotifications(newNotifications: List<PaymentNotification>) {
        notifications = newNotifications
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}
