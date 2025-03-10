package com.example.pathfide.Fragments.Chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pathfide.Adapter.RecentChatsAdapter
import com.example.pathfide.Adapter.UsersAdapter
import com.example.pathfide.Model.User
import com.example.pathfide.ViewModel.ChatViewModel
import com.example.pathfide.databinding.FragmentChatMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatMainFragment : Fragment() {

    private var _binding: FragmentChatMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var usersAdapter: UsersAdapter
    private lateinit var recentChatsAdapter: RecentChatsAdapter
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var firestore: FirebaseFirestore
    private val userProfiles = mutableMapOf<String, User>()
    private lateinit var searchTextWatcher: TextWatcher

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore and ViewModel
        firestore = Firebase.firestore
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        setupAdapters()
        observeViewModel()
        setupSearchFunctionality()
    }

    private fun setupAdapters() {
        // UsersAdapter for user search
        usersAdapter = UsersAdapter { selectedUser ->
            // Navigate to ChatFragment and pass user details
            val action = ChatMainFragmentDirections.actionChatMainFragmentToChatFragment(selectedUser.id)
            findNavController().navigate(action)
        }

        recentChatsAdapter = RecentChatsAdapter { selectedChat ->
            // Simply navigate without triggering any data refresh
            val action = ChatMainFragmentDirections
                .actionChatMainFragmentToChatFragment(selectedChat.id)
            findNavController().navigate(action)
        }

        binding.rvRecentChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentChatsAdapter
            // Add this to prevent layout jumps
            itemAnimator = null
        }

        binding.rvUserSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usersAdapter
        }
    }

    private fun observeViewModel() {
        chatViewModel.recentChats.observe(viewLifecycleOwner) { recentChats ->
            Log.d("ChatMainFragment", "Recent chats: $recentChats")
            recentChatsAdapter.submitList(recentChats)

            // Fetch user data for each chat
            recentChats.forEach { chat ->
                fetchUserForChat(chat.userId) // Just call the function
            }

            binding.rvRecentChats.visibility = if (recentChats.isNotEmpty()) View.VISIBLE else View.GONE
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ChatMainFragment", "User ID: $userId")
        userId?.let {
            chatViewModel.fetchRecentChats(it)
        } ?: Log.e("ChatMainFragment", "User ID is null, user might not be authenticated.")
    }

    private fun setupSearchFunctionality() {
        searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    fetchUsers(query)
                    binding.rvRecentChats.visibility = View.GONE
                    binding.rvUserSearchResults.visibility = View.VISIBLE
                } else {
                    usersAdapter.setUsers(emptyList())
                    binding.rvUserSearchResults.visibility = View.GONE
                    binding.rvRecentChats.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etSearchInput.addTextChangedListener(searchTextWatcher)
    }


    private fun fetchUserForChat(chatId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(chatId)

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Fetching user data
                val user = documentSnapshot.toObject(User::class.java)

                // Set the ID here
                user?.id = documentSnapshot.id // Ensure ID is set

                if (user != null) {
                    // Update userProfiles and notify the adapter
                    userProfiles[chatId] = user
                    recentChatsAdapter.setUserProfile(chatId, user) // Notify the adapter with the correct user
                } else {
                    Log.e("ChatMainFragment", "User not found for ID: $chatId")
                }
            } else {
                Log.d("ChatMainFragment", "User profile does not exist for chatId: $chatId")
            }
        }.addOnFailureListener { exception ->
            Log.d("ChatMainFragment", "Error fetching user profile: ${exception.message}")
        }
    }

    private fun fetchUsers(query: String) {
        // Get current user ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firestore.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = querySnapshot.documents.mapNotNull { document ->
                    // Skip if this is the current user
                    if (document.id != currentUserId) {
                        document.toObject(User::class.java)?.apply {
                            id = document.id
                        }
                    } else {
                        null
                    }
                }

                Log.d("ChatMainFragment", "Total users fetched (excluding current user): ${userList.size}")
                usersAdapter.setUsers(userList)
                usersAdapter.filter(query)

                // Show the user search results RecyclerView
                binding.rvUserSearchResults.visibility = if (userList.isNotEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { exception ->
                Log.e("ChatMainFragment", "Error fetching users", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        // 1️⃣ Clear the search input
        binding.etSearchInput.text?.clear()

        // 2️⃣ Clear the search results list
        usersAdapter.setUsers(emptyList()) // Removes all previous search results
        usersAdapter.notifyDataSetChanged() // Ensure the RecyclerView updates

        // 3️⃣ Ensure only recent chats are shown
        binding.rvRecentChats.visibility = View.VISIBLE
        binding.rvUserSearchResults.visibility = View.GONE
    }


}
