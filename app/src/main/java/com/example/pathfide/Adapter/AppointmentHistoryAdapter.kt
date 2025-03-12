package com.example.pathfide.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Model.ScheduledSession
import com.example.pathfide.R

class AppointmentHistoryAdapter(private val userType: String) : RecyclerView.Adapter<AppointmentHistoryAdapter.ViewHolder>() {
    private var sessions: List<ScheduledSession> = emptyList()

    fun submitList(newSessions: List<ScheduledSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.bind(session)
    }

    override fun getItemCount(): Int = sessions.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvSessionType: TextView = itemView.findViewById(R.id.tvSessionType)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus) // Added this


        fun bind(session: ScheduledSession) {
            val displayName = when (userType) {
                "THERAPIST" -> "${session.firstName} ${session.lastName}"
                else -> "${session.firstName} ${session.lastName}"
            }

            tvName.text = displayName
            tvSessionType.text = session.sessionType


            val dateText = session.appointmentDate
            tvDate.text = dateText

            val timeText = session.appointmentTime
            tvTime.text = timeText

            val status = when (session.isAccepted) {
                true -> {
                    tvStatus.setTextColor(itemView.context.getColor(R.color.green))
                    "Accepted"
                }
                false -> {
                    tvStatus.setTextColor(itemView.context.getColor(R.color.red))
                    "Declined"
                }
                null -> {
                    tvStatus.setTextColor(itemView.context.getColor(R.color.navy_blue))
                    "Pending"
                }
            }
            tvStatus.text = status
            // Set Payment Status with color coding
            val paymentText = when (session.paymentStatus) {
                "Paid" -> {
                    tvPaymentStatus.setTextColor(itemView.context.getColor(R.color.green))
                    "Paid"
                }
                else -> {
                    tvPaymentStatus.setTextColor(itemView.context.getColor(R.color.orange))
                    "Pending"
                }
            }
            tvPaymentStatus.text = paymentText

        }

    }
}
