package com.example.pathfide.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.R

class BookingNoticeAdapter(private val bookings: MutableList<String>) : RecyclerView.Adapter<BookingNoticeAdapter.BookingViewHolder>() {

    // ViewHolder for your item layout
    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookingTitle: TextView = itemView.findViewById(R.id.bookingTitle)
        private val bookingMessage: TextView = itemView.findViewById(R.id.bookingMessage)
        private val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)

        fun bind(booking: String) {
            val parts = booking.split("|") // Assuming you separate using "|"
            if (parts.size > 0) {
                bookingTitle.text = parts[0]
            } else {
                bookingTitle.text = "No Title"
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking_notice, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size


    fun addBooking(booking: String) {
        Log.d("BookingNoticeAdapter", "Booking added: $booking") // Log added
        bookings.add(booking)
        notifyDataSetChanged()
    }
}
