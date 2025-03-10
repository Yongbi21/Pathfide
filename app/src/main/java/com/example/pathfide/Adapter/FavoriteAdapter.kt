package com.example.pathfide.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.R
import com.example.pathfide.models.FavoriteItem

class FavoriteAdapter(
    private val favoriteList: MutableList<FavoriteItem>,
    private val onFavoriteRemoved: (String) -> Unit // Lambda to handle removing favorites
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.favoriteTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.favoriteDescription)
        val removeFavoriteStar: ImageButton = view.findViewById(R.id.removeFavoriteStar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_tips_item, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favoriteList[position]
        holder.titleTextView.text = favorite.title
        holder.descriptionTextView.text = favorite.description

        // Set an onClickListener to remove the item from favorites
        holder.removeFavoriteStar.setOnClickListener {
            onFavoriteRemoved(favorite.favoriteId)
            removeAt(position)
        }
    }

    override fun getItemCount(): Int = favoriteList.size

    // Optional: Method to remove item from the adapter if needed
    private fun removeAt(position: Int) {
        if (position < favoriteList.size) {
            favoriteList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
