    package com.example.pathfide.fragments

    import android.app.AlertDialog
    import android.graphics.Color
    import android.graphics.Typeface
    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.TextView
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.lifecycle.Observer
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.pathfide.Adapter.PostAdapter
    import com.example.pathfide.Model.Post
    import com.example.pathfide.R
    import com.example.pathfide.ViewModel.PostViewModel
    import com.google.android.material.floatingactionbutton.FloatingActionButton
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.google.android.material.textfield.TextInputEditText

    class ThreadsFragment : Fragment() {

        private val viewModel: PostViewModel by viewModels()
        private lateinit var adapter: PostAdapter
        private lateinit var recyclerView: RecyclerView
        private lateinit var fabNewPost: FloatingActionButton

        private val prohibitedWords = listOf(
            "kill", "murder", "blood", "attack", "weapon", "assault", "hurt",
            "beat", "stab", "slaughter", "threat", "bomb", "torture", "abuse",
            "execute", "carnage", "suicide", "self-harm", "cut", "overdose",
            "hang", "hangman", "depressed", "end it all", "take my life",
            "no hope", "end my pain", "despair", "worthless", "porn", "sex",
            "naked", "fetish", "orgasm", "adult", "lewd", "sexual", "erotica",
            "innuendo", "harassment", "obscene", "pervert", "I wish I were dead",
            "just end it", "kill yourself", "not worth living",
            "no one cares", "you're useless"
        )

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_post, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            recyclerView = view.findViewById(R.id.recyclerViewPosts)
            fabNewPost = view.findViewById(R.id.fabNewPost)

            setupRecyclerView()
            setupObservers()
            setupListeners()

            viewModel.fetchPosts() // Fetch posts when the fragment is created
        }

        private fun setupRecyclerView() {
            adapter = PostAdapter(
                onPostClicked = { post -> openCommentFragment(post) },
                onLikeClicked = { post -> viewModel.likePost(post.id, post.isLiked) },
                onDeleteClicked = { post -> showDeleteConfirmationDialog(post) },
                viewModel = viewModel

            )
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }

        private fun showDeleteConfirmationDialog(post: Post) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deletePost(post.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun setupObservers() {
            viewModel.posts.observe(viewLifecycleOwner, Observer { posts ->
                posts.forEach { post ->
                    post.isLiked = post.likedBy.contains(viewModel.auth.currentUser?.uid)
                    post.likeCount = post.likedBy.size // Set likeCount based on likedBy
                }
                adapter.submitList(posts) // Update the adapter's list
            })

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                // TODO: Show/hide loading indicator
            }
        }

        private fun setupListeners() {
            fabNewPost.setOnClickListener {
                showCreatePostFullScreenDialog()
            }
        }



        private fun showCreatePostFullScreenDialog() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_create_post, null)

            val editText = dialogView.findViewById<TextInputEditText>(R.id.post_content_edit_text)
            val postButton = dialogView.findViewById<Button>(R.id.post_button)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
            val charCountTextView = dialogView.findViewById<TextView>(R.id.char_count_text_view)

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val charCount = s?.length ?: 0
                    charCountTextView.text = "$charCount/200"
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            val dialog = AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
                .setView(dialogView)
                .create()

            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.show()

            postButton.setOnClickListener {
                val content = editText.text.toString()
                if (content.isNotBlank()) {
                    if (containsProhibitedWords(content)) {
                        showWarningDialog()
                    } else {
                        viewModel.createPost(content)
                        dialog.dismiss()
                    }
                } else {
                    // Show a toast message if the content is empty
                    Toast.makeText(requireContext(), "Please input something to post.", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }


        private fun openCommentFragment(post: Post) {
            val action = ThreadsFragmentDirections.actionThreadsFragmentToCommentFragment(postId = post.id)
            findNavController().navigate(action)
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
