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
import com.example.pathfide.databinding.FragmentStressBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StressFragment : Fragment() {

    private var _binding: FragmentStressBinding? = null
    private val binding get() = _binding!!

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    private val favoriteStates = mutableMapOf(
        "stress1" to false,
        "stress2" to false,
        "stress3" to false,
        "stress4" to false,
        "stress5" to false,
        "stress6" to false,
        "stress7" to false

    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStressBinding.inflate(inflater, container, false)
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
            binding.stress1.findViewById(R.id.starstress1),
            "stress1",
            "",
            "Develop healthy physical habits. Healthy eating, physical activity, and regular sleep can improve your physical and mental health."
        )
        handleStarClick(
            binding.stress2.findViewById(R.id.starstress2),
            "stress2",
            "",
            "Take time for yourself. Make taking care of yourself part of your daily routine. Take time to notice the good moments or do something that you enjoy, like reading a book or listening to music."
        )
        handleStarClick(
            binding.stress3.findViewById(R.id.starstress3),
            "stress3",
            "",
            "Look at problems from different angles. Think of challenging situations as growth opportunities. Try to see the positive side of things. Learn from your mistakes and don’t dwell on them."
        )
        handleStarClick(
            binding.stress4.findViewById(R.id.starstress4),
            "stress4",
            "",
            "Practice gratitude. Take time to note things to be thankful for each day."
        )
        handleStarClick(
            binding.stress5.findViewById(R.id.starstress5),
            "stress5",
            "",
            "Explore your beliefs about the meaning and purpose of life. Think about how to guide your life by the principles that are important to you."
        )
        handleStarClick(
            binding.stress6.findViewById(R.id.starstress6),
            "stress6",
            "",
            "Tap into your social connections and community. Surround yourself with positive, healthy people. Ask friends, family, or trusted members of your community for information or assistance when you need it. Look for cultural practices that you feel help in times of stress."
        )
        handleStarClick(
            binding.stress7.findViewById(R.id.starstress7),
            "stress7",
            "",
            "Get help for mental health and substance use disorders. Talk with a health care professional if you’re having trouble coping. Or call SAMHSA’s free national helpline at 1-800-662-HELP. If you or someone you know is thinking about suicide, you can call the National Suicide Prevention Lifeline at 1-800-273-TALK. You can also text “HOME” to the Crisis Text Line at 741741."
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
                "stress1" -> binding.stress1.findViewById<ImageButton>(R.id.starstress1)
                "stress2" -> binding.stress2.findViewById<ImageButton>(R.id.starstress2)
                "stress3" -> binding.stress3.findViewById<ImageButton>(R.id.starstress3)
                "stress4" -> binding.stress4.findViewById<ImageButton>(R.id.starstress4)
                "stress5" -> binding.stress5.findViewById<ImageButton>(R.id.starstress5)
                "stress6" -> binding.stress5.findViewById<ImageButton>(R.id.starstress6)
                "stress7" -> binding.stress5.findViewById<ImageButton>(R.id.starstress7)
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
