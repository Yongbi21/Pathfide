package com.example.pathfide.Adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Model.ScheduledSession
import com.example.pathfide.R


class ClientScheduledSessionsAdapter(
    private val sessions: List<ScheduledSession>,
    private val onSessionClick: (ScheduledSession) -> Unit
) : RecyclerView.Adapter<ClientScheduledSessionsAdapter.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ACCEPTED = 0
        private const val VIEW_TYPE_DECLINED = 1
        private const val VIEW_TYPE_PENDING = 2
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.notificationTitle)
        val description: TextView = view.findViewById(R.id.notificationMessage)
        val icon: ImageView = view.findViewById(R.id.notificationIcon)
        val cardView: CardView = view.findViewById(R.id.notificationCard)
        // Make rebookLabel nullable since it might not exist in all layouts
        val rebookLabel: TextView? = view.findViewById(R.id.rebookLabel)
    }

    override fun getItemViewType(position: Int): Int {
        return when (sessions[position].isAccepted) {
            true -> VIEW_TYPE_ACCEPTED
            false -> VIEW_TYPE_DECLINED
            null -> VIEW_TYPE_PENDING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = when (viewType) {
            VIEW_TYPE_ACCEPTED -> R.layout.item_accepted
            VIEW_TYPE_DECLINED -> R.layout.item_decline
            else -> R.layout.item_pending
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]

        // Safely handle the rebookLabel visibility
        val isRebooked = session.isAccepted == null && session.isRebooking == true
        holder.rebookLabel?.visibility = if (isRebooked) View.VISIBLE else View.GONE

        holder.title.text = when (session.isAccepted) {
            true -> "Appointment Accepted"
            false -> "Appointment Declined"
            null -> "Appointment Pending"
        }

        val fullText = when (session.isAccepted) {
            true -> "Hello ${session.firstName} ${session.lastName}, your appointment on ${session.appointmentDate} " +
                    "at ${session.appointmentTime} has been accepted. Please proceed with the payment " +
                    "to successfully book your session and wait for the admin for the confirmation. Thank you!"
            false -> "Hello ${session.firstName} ${session.lastName}, we regret to inform you that your " +
                    "appointment on ${session.appointmentDate} at ${session.appointmentTime} has been declined. " +
                    "We encourage you to explore other professionals available for booking."
            null -> "Hello ${session.firstName} ${session.lastName}, your appointment request for " +
                    "${session.appointmentDate} at ${session.appointmentTime} is pending for approval. " +
                    "We'll notify you once the therapist responds."
        }

        val spannable = SpannableString(fullText)

        applyBoldStyle(spannable, fullText, "${session.firstName} ${session.lastName}")
        applyBoldStyle(spannable, fullText, session.appointmentDate)
        applyBoldStyle(spannable, fullText, session.appointmentTime)
        session.sessionType?.let { applyBoldStyle(spannable, fullText, it) }

        holder.description.text = spannable

        holder.icon.setImageResource(when (session.isAccepted) {
            true -> R.drawable.check
            false -> R.drawable.decline
            null -> R.drawable.warning
        })

        // Handle card click based on acceptance status
        holder.cardView.apply {
            val isClickable = session.isAccepted == true
            this.isClickable = isClickable
            this.isFocusable = isClickable
            setOnClickListener(if (isClickable) { _ -> onSessionClick(session) } else null)
        }
    }

    private fun applyBoldStyle(spannable: SpannableString, fullText: String, textToBold: String) {
        val startIndex = fullText.indexOf(textToBold)
        if (startIndex >= 0) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                startIndex + textToBold.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun getItemCount(): Int = sessions.size
}