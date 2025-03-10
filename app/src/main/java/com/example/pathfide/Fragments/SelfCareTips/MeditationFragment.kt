package com.example.pathfide.fragments

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pathfide.R
import com.example.pathfide.databinding.FragmentMeditationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MeditationFragment : Fragment() {

    private var _binding: FragmentMeditationBinding? = null
    private val binding get() = _binding!!

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    private val favoriteStates = mutableMapOf(
        "meditation1" to false,
        "meditation2" to false,
        "meditation3" to false,
        "meditation4" to false,
        "meditation5" to false,
        "meditation6" to false,
        "meditation7" to false,

    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeditationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        loadFavorites()
        val infoIcon: ImageView = view.findViewById(R.id.infoIcon)

        // Show tooltip when clicking the info icon
        infoIcon.setOnClickListener {
            showTooltip(it, "National Institutes of Health (NIH). (2023). Your healthiest self: Wellness toolkits.")
        }
    }

    private fun showTooltip(anchorView: View, message: String) {
        val inflater = LayoutInflater.from(anchorView.context)
        val tooltipView = inflater.inflate(R.layout.tooltip_layout, null)

        val popupWindow = PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set tooltip text
        tooltipView.findViewById<TextView>(R.id.tooltipText).text = message

        // Set background (optional)
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(anchorView.context, R.drawable.tooltip_background))

        // Show the tooltip near the icon
        popupWindow.showAsDropDown(anchorView, 0, -anchorView.height - 10, Gravity.CENTER)
    }

    private fun setupClickListeners() {
        handleStarClick(
            binding.meditation1.findViewById(R.id.starmed1),
            "meditation1",
            "",
            "Take care of yourself. Try to eat right, exercise, and get enough sleep. Avoid bad habits—like smoking or drinking alcohol—that can put your health at risk.\n"
        )

        handleStarClick(
            binding.meditation2.findViewById(R.id.starmed2),
            "meditation2",
            "",
            "Talk to caring friends. Let others know when you want to talk."
        )
        handleStarClick(
            binding.meditation3.findViewById(R.id.starmed3),
            "meditation3",
            "",
            "Find a grief support group. It might help to talk with others who are also grieving."
        )
        handleStarClick(
            binding.meditation4.findViewById(R.id.starmed4),
            "meditation4",
            "",
            "Don’t make major changes right away. Wait a while before making big decisions like moving or changing jobs."
        )
        handleStarClick(
            binding.meditation5.findViewById(R.id.starmed5),
            "meditation5",
            "",
            "Talk to your doctor if you’re having trouble with everyday activities."
        )
        handleStarClick(
            binding.meditation6.findViewById(R.id.starmed6),
            "meditation6",
            "",
            "Consider additional support. Sometimes short-term talk therapy can help."
        )
        handleStarClick(
            binding.meditation7.findViewById(R.id.starmed7),
            "meditation7",
            "",
            "Be patient. Mourning takes time. It’s common to have roller-coaster emotions for a while"
        )
    }

    private fun handleStarClick(
        starButton: ImageButton,
        favoriteId: String,
        title: String,
        description: String
    ) {
        starButton.setOnClickListener {
            val isFilled = favoriteStates[favoriteId] ?: false
            favoriteStates[favoriteId] = !isFilled // Toggle state

            if (!isFilled) {
                starButton.setImageResource(R.drawable.fill_star)
                saveFavorite(favoriteId, title, description) // Add to favorites
            } else {
                starButton.setImageResource(R.drawable.default_star)
                removeFavorite(favoriteId) // Remove from favorites
            }
        }
    }

    private fun loadFavorites() {
        userId?.let {
            db.collection("users").document(it).collection("favorites")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val favoriteId = document.getString("favoriteId")
                        if (favoriteId != null) {
                            favoriteStates[favoriteId] = true // Mark as filled
                        }
                    }
                    updateStarButtons() // Update UI after loading
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error loading favorites", e)
                }
        }
    }

    private fun updateStarButtons() {
        for ((favoriteId, isFilled) in favoriteStates) {
            val starButton = when (favoriteId) {
                "meditation1" -> binding.meditation1.findViewById<ImageButton>(R.id.starmed1)
                "meditation2" -> binding.meditation2.findViewById<ImageButton>(R.id.starmed2)
                "meditation3" -> binding.meditation3.findViewById<ImageButton>(R.id.starmed3)
                "meditation4" -> binding.meditation4.findViewById<ImageButton>(R.id.starmed4)
                "meditation5" -> binding.meditation1.findViewById<ImageButton>(R.id.starmed5)
                "meditation6" -> binding.meditation2.findViewById<ImageButton>(R.id.starmed6)
                "meditation7" -> binding.meditation3.findViewById<ImageButton>(R.id.starmed7)
                else -> null
            }

            if (starButton != null) {
                starButton.setImageResource(if (isFilled) R.drawable.fill_star else R.drawable.default_star)
            }
        }
    }

    private fun saveFavorite(favoriteId: String, title: String, description: String) {
        userId?.let {
            val favoriteData = hashMapOf(
                "favoriteId" to favoriteId,
                "title" to title,
                "description" to description
            )
            db.collection("users").document(it).collection("favorites")
                .document(favoriteId)
                .set(favoriteData)
                .addOnSuccessListener {
                    Log.d("Firestore", "Favorite added: $favoriteId")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error adding favorite: $favoriteId", e)
                }
        }
    }

    private fun removeFavorite(favoriteId: String) {
        userId?.let {
            db.collection("users").document(it).collection("favorites")
                .document(favoriteId)
                .delete()
                .addOnSuccessListener {
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
