package com.example.pathfide.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Model.Comment
import com.example.pathfide.R
import com.bumptech.glide.Glide

class CommentAdapter(


    private val onLikeClicked: (Comment) -> Unit,
    private val onReplyClicked: (Comment) -> Unit
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.commentProfileImageView)
        private val textViewFirstName: TextView = itemView.findViewById(R.id.commentFirstName)
        private val textViewLastName: TextView = itemView.findViewById(R.id.commentLastName)
        private val textViewComment: TextView = itemView.findViewById(R.id.commentTextView)
        private val likeButton: ImageButton = itemView.findViewById(R.id.buttonLike)
        private val likeCountTextView: TextView = itemView.findViewById(R.id.textViewLikeCount)
        private val replyButton: TextView = itemView.findViewById(R.id.commentReplyTextView)

        fun bind(comment: Comment) {
            textViewFirstName.text = comment.firstName
            textViewLastName.text = comment.lastName
            textViewComment.text = comment.content

            // Load profile image using Glide
            if (comment.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(comment.profileImageUrl)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile)
            }

            // Indent replies to differentiate them visually
            if (comment.parentCommentId != null) {
                itemView.setPadding(250, 30, 0, 30)  // Indent replies (this can be adjusted)
            } else {
                itemView.setPadding(70, 75, 0, 10)  // No indent for top-level comments
            }

            updateLikeButton(comment)

            likeButton.setOnClickListener {
                comment.isLiked = !comment.isLiked
                if (comment.isLiked) {
                    comment.likeCount++
                } else {
                    comment.likeCount = maxOf(0, comment.likeCount - 1)
                }
                onLikeClicked(comment)
                updateLikeButton(comment)
            }

            replyButton.setOnClickListener {
                onReplyClicked(comment)
            }
        }

        private fun updateLikeButton(comment: Comment) {
            if (comment.isLiked) {
                likeButton.setImageResource(R.drawable.ic_heart_filled)
            } else {
                likeButton.setImageResource(R.drawable.ic_heart)
            }
            likeCountTextView.text = comment.likeCount.toString()
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }


}