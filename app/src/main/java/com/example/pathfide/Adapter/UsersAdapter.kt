package com.example.pathfide.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pathfide.Model.User
import com.example.pathfide.R
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(private val onUserClicked: (User) -> Unit) :
    ListAdapter<User, UsersAdapter.ViewHolder>(UserDiffCallback()) {

    private var fullList: List<User> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)

        // Handle user click
        holder.itemView.setOnClickListener {
            Log.d("UsersAdapter", "User clicked: ${user.firstName} ${user.lastName} with ID: ${user.id}")
            onUserClicked(user)
        }
    }

    fun setUsers(users: List<User>) {
        fullList = users
        Log.d("UsersAdapter", "Full user list set with size: ${fullList.size}")
        fullList.forEach { user ->
            Log.d("UsersAdapter", "User in fullList: ${user.firstName.trim()} ${user.lastName.trim()}")
        }
        submitList(users) // Use 'users' directly
    }

    fun filter(query: String) {
        val trimmedQuery = query.trim()
        Log.d("UsersAdapter", "Starting filter with query: $trimmedQuery")

        val filteredList = if (trimmedQuery.isEmpty()) {
            emptyList() // Show an empty list when the input is cleared
        } else {
            fullList.filter { user ->
                val fullName = "${user.firstName.trim()} ${user.lastName.trim()}"

                // Log the full name and query comparison
                Log.d("UsersAdapter", "Checking user: $fullName against query '$trimmedQuery'")

                fullName.contains(trimmedQuery, ignoreCase = true) ||
                        user.firstName.trim().contains(trimmedQuery, ignoreCase = true) ||
                        user.lastName.trim().contains(trimmedQuery, ignoreCase = true)
            }.also { list ->
                Log.d("UsersAdapter", "Filtered users: ${list.size} matching for query '$trimmedQuery'")
            }
        }

        submitList(filteredList)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: CircleImageView = itemView.findViewById(R.id.imageViewProfile)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameText)
        private val statusImageView: ImageView = itemView.findViewById(R.id.statusOnline)

        fun bind(user: User) {
            nameTextView.text = "${user.firstName} ${user.lastName}"

            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.person) // Fallback image
                .into(avatarImageView)

            statusImageView.visibility = if (user.isOnline) View.VISIBLE else View.GONE
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
