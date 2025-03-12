package com.example.pathfide.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Model.Post
import com.example.pathfide.R
import com.bumptech.glide.Glide
import com.example.pathfide.ViewModel.PostViewModel


class PostAdapter(
    private val onPostClicked: (Post) -> Unit,
    private val onLikeClicked: (Post) -> Unit,
    private val onDeleteClicked: (Post) -> Unit,
    private val viewModel: PostViewModel

) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
        holder.itemView.findViewById<ImageButton>(R.id.buttonPostMenu).setOnClickListener {
            showPopupMenu(it, post)
        }
    }
    private fun showPopupMenu(view: View, post: Post) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.post_options_menu, popupMenu.menu)

        val deleteMenuItem = popupMenu.menu.findItem(R.id.menu_delete) // Find delete option

        val currentUserId = viewModel.getCurrentUserId() // Get logged-in user ID

        // Check if the logged-in user is the post owner
        val isOwner = post.userId == currentUserId

        // Disable the delete option for non-owners
        deleteMenuItem.isEnabled = isOwner

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    if (isOwner) {
                        onDeleteClicked(post)
                    } else {
                        Toast.makeText(view.context, "You cannot delete this post.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameProfile)
        private val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameProfile)
        private val contentTextView: TextView = itemView.findViewById(R.id.textViewContent)
        private val likeButton: ImageButton = itemView.findViewById(R.id.buttonLike)
        private val likeCountTextView: TextView = itemView.findViewById(R.id.textViewLikeCount)
        private val commentCountTextView: TextView = itemView.findViewById(R.id.textViewCommentCount) // New line
        private val timeAgoTextView: TextView = itemView.findViewById(R.id.textViewTimeAgo)

        fun bind(post: Post) {
            firstNameTextView.text = post.firstName
            lastNameTextView.text = post.lastName
            contentTextView.text = post.content
            commentCountTextView.text = "Comments: ${post.commentCount}" // Set comment count
            timeAgoTextView.text = viewModel.getTimeAgo(post.timestamp)


            // Load profile image using Glide or Picasso
            if (post.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context) // Or Picasso.get().load(post.profileImageUrl).into(profileImageView)
                    .load(post.profileImageUrl)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile) // Set a default image if none
            }

            // Set the like button based on whether the post is liked
            updateLikeButton(post)

            likeButton.setOnClickListener {
                post.isLiked = !post.isLiked // Toggle the local state
                if (post.isLiked) {
                    post.likeCount++
                } else {
                    post.likeCount = maxOf(0, post.likeCount - 1)
                }

                onLikeClicked(post) // Notify ViewModel to update the like status in Firestore
                updateLikeButton(post) // Update button state
            }

            itemView.findViewById<ImageButton>(R.id.buttonComment).setOnClickListener {
                onPostClicked(post)
            }
        }



        private fun updateLikeButton(post: Post) {
            if (post.isLiked) {
                likeButton.setImageResource(R.drawable.ic_heart_filled)
            } else {
                likeButton.setImageResource(R.drawable.ic_heart)
            }
            likeCountTextView.text = post.likeCount.toString()
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
