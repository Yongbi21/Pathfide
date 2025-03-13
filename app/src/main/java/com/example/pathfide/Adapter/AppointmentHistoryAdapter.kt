package com.example.pathfide.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Model.ScheduledSession
import com.example.pathfide.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
        private val tvPaymentStatus: TextView =
            itemView.findViewById(R.id.tvPaymentStatus) // Added this
        private val btnTherapistAction: ImageButton = itemView.findViewById(R.id.btnTherapistAction)
        private val imgStatusChanged: ImageView = itemView.findViewById(R.id.imgStatusChanged)


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

            // ✅ Set visibility based on sessionStatus
            if (session.sessionStatus == "Session Done") {
                btnTherapistAction.visibility = View.GONE
                imgStatusChanged.visibility = View.VISIBLE
            } else {
                if (userType == "THERAPIST") {
                    btnTherapistAction.visibility = View.VISIBLE
                    imgStatusChanged.visibility = View.GONE
                } else {
                    btnTherapistAction.visibility = View.GONE
                    imgStatusChanged.visibility = View.GONE
                }
            }

            btnTherapistAction.setOnClickListener {
                addSessionStatus(session)
            }
        }


    private fun addSessionStatus(session: ScheduledSession) {
        val db = FirebaseFirestore.getInstance()
        val sessionRef = db.collection("scheduledSessions").document(session.appointmentId)

        val updateData = hashMapOf("sessionStatus" to "Session Done")

        sessionRef.set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(itemView.context, "Session Done!", Toast.LENGTH_SHORT).show()

                // ✅ Update the session object in the list
                session.sessionStatus = "Session Done"

                // ✅ Refresh RecyclerView without refetching data
                notifyItemChanged(adapterPosition)
            }
            .addOnFailureListener { e ->
                Toast.makeText(itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    }
}
