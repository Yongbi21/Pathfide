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
import com.example.pathfide.databinding.FragmentCopingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CopingFragment : Fragment() {

    private var _binding: FragmentCopingBinding? = null
    private val binding get() = _binding!!

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    private val favoriteStates = mutableMapOf(
        "coping1" to false,
        "coping2" to false,
        "coping3" to false,
        "coping4" to false,
        "coping5" to false,
        "coping6" to false,
        "coping7" to false,
        "coping8" to false,
        "coping9" to false,
        "coping10" to false,

        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCopingBinding.inflate(inflater, container, false)
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
            binding.coping1.findViewById(R.id.copingSelfLoss1),
            "coping1",
            "",
            "Stick to a sleep schedule. Go to bed and wake up at the same time every day, even on the weekends."

        )
        handleStarClick(
            binding.coping2.findViewById(R.id.copingSelfLoss2),
            "coping2",
            "",
            "Get some exercise every day. But not close to bedtime."

        )
        handleStarClick(
            binding.coping3.findViewById(R.id.copingSelfLoss3),
            "coping3",
            "",
            "Go outside. Try to get natural sunlight for at least 30 minutes every day."

        )
        handleStarClick(
            binding.coping4.findViewById(R.id.copingSelfLoss4),
            "coping4",
            "",
            "Avoid nicotine and caffeine. Both are stimulants that keep you awake. Caffeine can take 6–8 hours to wear off completely."

        )
        handleStarClick(
            binding.coping5.findViewById(R.id.copingSelfLoss5),
            "coping5",
            "",
            "Don’t take naps after mid-afternoon. And keep them short."

        )
        handleStarClick(
            binding.coping6.findViewById(R.id.copingSelfLoss6),
            "coping6",
            "",
            "Don’t take naps after mid-afternoon. And keep them short."

        )
        handleStarClick(
            binding.coping7.findViewById(R.id.copingSelfLoss7),
            "coping7",
            "",
            "Avoid alcohol and large meals before bedtime. Both can prevent deep, restorative sleep."

        )
        handleStarClick(
            binding.coping8.findViewById(R.id.copingSelfLoss8),
            "coping8",
            "",
            "Limit electronics before bed. Try reading a book, listening to soothing music, or another relaxing activity instead."

        )
        handleStarClick(
            binding.coping9.findViewById(R.id.copingSelfLoss9),
            "coping9",
            "",
            "Don’t lie in bed awake. If you can’t fall asleep after 20 minutes, get up and do a relaxing activity until you feel sleepy again."
        )
        handleStarClick(
            binding.coping10.findViewById(R.id.copingSelfLoss10),
            "coping10",
            "",
            "See your health care provider if nothing you try helps. They can determine if you need further testing. They can also help you learn new ways to manage stress."
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
                "coping1" -> binding.coping1.findViewById<ImageButton>(R.id.copingSelfLoss1)
                "coping2" -> binding.coping2.findViewById<ImageButton>(R.id.copingSelfLoss2)
                "coping3" -> binding.coping3.findViewById<ImageButton>(R.id.copingSelfLoss3)
                "coping4" -> binding.coping4.findViewById<ImageButton>(R.id.copingSelfLoss4)
                "coping5" -> binding.coping5.findViewById<ImageButton>(R.id.copingSelfLoss5)
                "coping6" -> binding.coping6.findViewById<ImageButton>(R.id.copingSelfLoss6)
                "coping7" -> binding.coping7.findViewById<ImageButton>(R.id.copingSelfLoss7)
                "coping8" -> binding.coping8.findViewById<ImageButton>(R.id.copingSelfLoss8)
                "coping9" -> binding.coping9.findViewById<ImageButton>(R.id.copingSelfLoss9)
                "coping10" -> binding.coping10.findViewById<ImageButton>(R.id.copingSelfLoss10)
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
