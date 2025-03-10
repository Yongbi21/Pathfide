package com.example.pathfide.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pathfide.Adapter.FavoriteAdapter
import com.example.pathfide.databinding.FragmentFavoritesBinding
import com.example.pathfide.models.FavoriteItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val favoriteList = mutableListOf<FavoriteItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView with adapter
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        favoriteAdapter = FavoriteAdapter(favoriteList) { favoriteId -> removeFavorite(favoriteId) }
        binding.recyclerViewFavorites.adapter = favoriteAdapter

        // Load favorites in real-time using Firestore snapshot listener
        loadFavoritesSnapshotListener()
    }

    private fun loadFavoritesSnapshotListener() {
        userId?.let { uid ->
            db.collection("users").document(uid).collection("favorites")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("FavoriteFragment", "Listen failed.", e)
                        return@addSnapshotListener
                    }
    
                    if (snapshot != null && !snapshot.isEmpty) {
                        favoriteList.clear() // Clear the list to avoid duplicates

                        for (document in snapshot.documents) {
                            val favoriteId = document.getString("favoriteId") ?: continue
                            val title = document.getString("title") ?: ""
                            val description = document.getString("description") ?: ""

                            val favoriteItem = FavoriteItem(favoriteId, title, description)
                            favoriteList.add(favoriteItem)
                        }

                        // Notify adapter that data has changed
                        favoriteAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("FavoriteFragment", "No favorites found")
                    }
                }
        }
    }

    private fun removeFavorite(favoriteId: String) {
        userId?.let { uid ->
            db.collection("users").document(uid).collection("favorites")
                .document(favoriteId)
                .delete()
                .addOnSuccessListener {
                    // Log success
                    Log.d("Firestore", "Favorite removed: $favoriteId")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error removing favorite: $favoriteId", e)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}