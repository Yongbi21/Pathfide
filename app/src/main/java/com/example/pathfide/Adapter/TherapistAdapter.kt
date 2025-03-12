package com.example.pathfide.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pathfide.Model.Therapist
import com.example.pathfide.R
import com.example.pathfide.databinding.ItemTherapistBinding

class TherapistAdapter(
    private var therapists: List<Therapist>,
    private val itemClickListener: OnItemClickListener // Add a listener parameter
) : RecyclerView.Adapter<TherapistAdapter.TherapistViewHolder>() {

    // Define the click listener interface
    interface OnItemClickListener {
        fun onItemClick(therapist: Therapist) // Click event for each therapist
    }

    // Define the ViewHolder class
    class TherapistViewHolder(private val binding: ItemTherapistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(therapist: Therapist, clickListener: OnItemClickListener) {
            binding.fullNameTherapist.text = therapist.fullName
            binding.descriptionMessage.text = therapist.description
            binding.clinicLocation.text = therapist.location

            // Load avatar image using Glide
            Glide.with(itemView.context)
                .load(therapist.avatarUrl) // Load the avatar URL
                .placeholder(R.drawable.person) // Optional placeholder image
                .into(binding.therapistImage)

            // Set the click listener for the entire card
            itemView.setOnClickListener {
                clickListener.onItemClick(therapist) // Trigger the click event
            }
        }
    }

    // Update data method to refresh the adapter
    fun updateData(newTherapists: List<Therapist>) {
        this.therapists = newTherapists
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TherapistViewHolder {
        val binding = ItemTherapistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TherapistViewHolder(binding)
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: TherapistViewHolder, position: Int) {
        holder.bind(therapists[position], itemClickListener) // Pass the click listener to the holder
    }

    // Return the size of the therapists list
    override fun getItemCount(): Int {
        return therapists.size // Use the therapists list directly
    }
}
