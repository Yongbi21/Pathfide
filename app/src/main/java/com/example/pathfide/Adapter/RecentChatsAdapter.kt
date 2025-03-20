package com.example.pathfide.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pathfide.Model.Chat
import com.example.pathfide.Model.User
import com.example.pathfide.R
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class RecentChatsAdapter(
    private val onChatClick: (Chat) -> Unit // Click listener passed from the Fragment
) : ListAdapter<Chat, RecentChatsAdapter.ViewHolder>(ChatDiffCallback()) {

    private val userProfiles: MutableMap<String, User> = mutableMapOf()

    fun setUserProfile(chatId: String, user: User) {
        userProfiles[chatId] = user
        // Find the position of the chat and update only that item
        currentList.indexOfFirst { it.userId == chatId }.takeIf { it != -1 }?.let { position ->
            notifyItemChanged(position)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = getItem(position)
        holder.bind(chat, userProfiles[chat.userId], onChatClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: CircleImageView = itemView.findViewById(R.id.recentChatImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.recentChatTextName)
        private val timeTextView: TextView = itemView.findViewById(R.id.recentChatTextTime)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.recentChatTextLastMessage)

        fun bind(chat: Chat, user: User?, onChatClick: (Chat) -> Unit) {
            nameTextView.text = user?.let { "${it.firstName} ${it.surName}${user.lastName}" } ?: "Unknown User"
            lastMessageTextView.text = chat.lastMessage

            // Format and set the time using the new method
            chat.lastMessageTimestamp?.let {
                timeTextView.text = formatTimestamp(it)
            }

            Glide.with(itemView)
                .load(user?.profileImageUrl ?: chat.profileImage)
                .placeholder(R.drawable.person)
                .into(imageView)

            itemView.setOnClickListener {
                onChatClick(chat)
            }
        }

        // New method to format the timestamp
        private fun formatTimestamp(timestamp: Date): String {
            val now = Date()
            val diff = now.time - timestamp.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val months = days / 30
            val years = months / 12

            return when {
                years > 0 -> "$years year${if (years > 1) "s" else ""} ago"
                months > 0 -> "$months month${if (months > 1) "s" else ""} ago"
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                else -> "Just now"
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            // Compare only the relevant fields that should trigger a refresh
            return oldItem.id == newItem.id &&
                    oldItem.lastMessage == newItem.lastMessage &&
                    oldItem.lastMessageTimestamp == newItem.lastMessageTimestamp &&
                    oldItem.userId == newItem.userId
        }
    }
}
