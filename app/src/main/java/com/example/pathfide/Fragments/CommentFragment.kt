package com.example.pathfide.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Adapter.CommentAdapter
import com.example.pathfide.Model.Comment
import com.example.pathfide.R
import com.example.pathfide.ViewModel.CommentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommentFragment : Fragment() {

    private val viewModel: CommentViewModel by activityViewModels()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var postId: String
    private var replyingToComment: Comment? = null

    private lateinit var commentInput: EditText
    private lateinit var buttonPostComment: ImageButton
    private lateinit var replyingToTextView: TextView

    private val prohibitedWords = listOf(
        "kill", "murder", "blood", "attack", "weapon", "assault", "hurt",
        "beat", "stab", "slaughter", "threat", "bomb", "torture", "abuse",
        "execute", "carnage", "suicide", "self-harm", "cut", "overdose",
        "hang", "hangman", "depressed", "end it all", "take my life",
        "no hope", "end my pain", "despair", "worthless", "porn", "sex",
        "naked", "fetish", "orgasm", "adult", "lewd", "sexual", "erotica",
        "innuendo", "harassment", "obscene", "pervert", "I wish I were dead",
        "just end it", "you should kill yourself", "it's not worth living",
        "no one cares", "you're useless"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getString("postId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentInput = view.findViewById(R.id.editTextAddComment)
        buttonPostComment = view.findViewById(R.id.buttonPostComment)
        replyingToTextView = view.findViewById(R.id.textViewReplyingTo)

        setupRecyclerView(view)

        viewModel.fetchComments(postId)
        viewModel.comments.observe(viewLifecycleOwner, Observer { comments ->
            commentAdapter.submitList(comments)
        })

        buttonPostComment.setOnClickListener {
            val commentText = commentInput.text.toString()
            if (commentText.isNotBlank()) {
                if (containsProhibitedWords(commentText)) {
                    showWarningDialog() // Show warning if comment contains prohibited words
                } else {
                    if (replyingToComment != null) {
                        viewModel.postReply(postId, replyingToComment!!.id, commentText)
                        clearReplyingTo()
                    } else {
                        viewModel.postComment(postId, commentText)
                    }
                    commentInput.text.clear()
                }
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewComments)
        commentAdapter = CommentAdapter(
            onLikeClicked = { comment ->
                viewModel.likeComment(postId, comment.id, comment.isLiked)
            },
            onReplyClicked = { comment ->
                setReplyingTo(comment)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = commentAdapter
    }

    private fun setReplyingTo(comment: Comment) {
        replyingToComment = comment
        replyingToTextView.visibility = View.VISIBLE
        replyingToTextView.text = "Replying to ${comment.firstName}'s comment"
        commentInput.hint = "Write a reply..."
        commentInput.requestFocus()
    }

    private fun clearReplyingTo() {
        replyingToComment = null
        replyingToTextView.visibility = View.GONE
        commentInput.hint = "Add a comment"
    }

    private fun containsProhibitedWords(content: String): Boolean {
        val lowerCasedContent = content.lowercase()
        return prohibitedWords.any { lowerCasedContent.contains(it) }
    }

    private fun showWarningDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_warning, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.warningTitle)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()

        titleTextView.setTextColor(Color.RED)
        titleTextView.setTypeface(null, Typeface.BOLD)
    }
}
