    package com.example.pathfide.Adapter

    import android.graphics.Typeface
    import android.text.Spannable
    import android.text.SpannableString
    import android.text.style.StyleSpan
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.cardview.widget.CardView
    import androidx.recyclerview.widget.RecyclerView
    import com.example.pathfide.Model.ScheduledSession
    import com.example.pathfide.R

    class TherapistScheduledSessionsAdapter(
        private val sessions: List<ScheduledSession>,
        private val onSessionClick: (ScheduledSession) -> Unit
    ) : RecyclerView.Adapter<TherapistScheduledSessionsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.notificationTitle)
            val description: TextView = view.findViewById(R.id.notificationMessage)
            val cardView: CardView = view.findViewById(R.id.notificationCard)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reminder, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val session = sessions[position]
            holder.title.text = "Appointment Request"

            val fullText = "You have an appointment request from ${session.firstName} ${session.lastName} on ${session.appointmentDate} at ${session.appointmentTime} for a ${session.sessionType}"
            val spannable = SpannableString(fullText)

            // Apply bold styling to specific parts
            applyBoldStyle(spannable, fullText, "${session.firstName} ${session.lastName}")
            applyBoldStyle(spannable, fullText, session.appointmentDate)
            applyBoldStyle(spannable, fullText, session.appointmentTime)
            applyBoldStyle(spannable, fullText, session.sessionType)

            holder.description.text = spannable

            // Set click listener on the card
            holder.cardView.setOnClickListener {
                onSessionClick(session)
            }

            // Add ripple effect
            holder.cardView.apply {
                isClickable = true
                isFocusable = true
            }
        }

        private fun applyBoldStyle(spannable: SpannableString, fullText: String, textToBold: String) {
            val startIndex = fullText.indexOf(textToBold)
            val endIndex = startIndex + textToBold.length
            if (startIndex >= 0) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        override fun getItemCount(): Int = sessions.size
    }