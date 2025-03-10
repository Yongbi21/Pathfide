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
import com.example.pathfide.databinding.FragmentSelfEsteemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelfEsteemFragment : Fragment() {

    private var _binding: FragmentSelfEsteemBinding? = null
    private val binding get() = _binding!!

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    private val favoriteStates = mutableMapOf(
        "selfEsteem1" to false,
        "selfEsteem2" to false,
        "selfEsteem3" to false,
        "selfEsteem4" to false,
        "selfEsteem5" to false,
        "selfEsteem6" to false,
        "selfEsteem7" to false
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfEsteemBinding.inflate(inflater, container, false)
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
            binding.starself1.findViewById(R.id.starself1),
            "selfEsteem1",
            "",
            "Get enough sleep. Adults need 7 or more hours each night, school-age kids need 9–12, and teens need 8–10.\n"
        )

        handleStarClick(
            binding.starself2.findViewById(R.id.starself2),
            "selfEsteem2",
            "",
            "Exercise regularly. Just 30 minutes a day of walking can boost mood and reduce stress."
        )
        handleStarClick(
            binding.starself3.findViewById(R.id.starself3),
            "selfEsteem3",
            "",
            "Build a social support network."
        )
        handleStarClick(
            binding.starself4.findViewById(R.id.starself4),
            "selfEsteem4",
            "",
            "Set priorities. Decide what must get done and what can wait. Say no to new tasks if you feel they’re too much."
        )
        handleStarClick(
            binding.starself5.findViewById(R.id.starself5),
            "selfEsteem5",
            "",
            "Show compassion for yourself. Note what you’ve accomplished at the end of the day, not what you didn’t."
        )
        handleStarClick(
            binding.starself6.findViewById(R.id.starself6),
            "selfEsteem6",
            "",
            "Schedule regular times for a relaxing activity that uses mindfulness/breathing exercises, like yoga or tai chi."
        )
        handleStarClick(
            binding.starself7.findViewById(R.id.starself7),
            "selfEsteem7",
            "",
            "Seek help. Talk to a mental health professional if you feel unable to cope, have suicidal thoughts, or use drugs or alcohol to cope."
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
                "selfEsteem1" -> binding.selfEsteem1.findViewById<ImageButton>(R.id.starself1)
                "selfEsteem2" -> binding.selfEsteem2.findViewById<ImageButton>(R.id.starself2)
                "selfEsteem3" -> binding.selfEsteem3.findViewById<ImageButton>(R.id.starself3)
                "selfEsteem4" -> binding.selfEsteem4.findViewById<ImageButton>(R.id.starself4)
                "selfEsteem5" -> binding.selfEsteem5.findViewById<ImageButton>(R.id.starself5)
                "selfEsteem6" -> binding.selfEsteem5.findViewById<ImageButton>(R.id.starself6)
                "selfEsteem7" -> binding.selfEsteem5.findViewById<ImageButton>(R.id.starself7)
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
