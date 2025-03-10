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
import com.example.pathfide.databinding.FragmentMindfulBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MindfulFragment : Fragment() {

    private var _binding: FragmentMindfulBinding? = null
    private val binding get() = _binding!!

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    private val favoriteStates = mutableMapOf(
        "mindfulness1" to false,
        "mindfulness2" to false,
        "mindfulness3" to false,
        "mindfulness4" to false,
        "mindfulness5" to false,

        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMindfulBinding.inflate(inflater, container, false)
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
            binding.mindfulness1.findViewById(R.id.mindfulnessMed1),
            "mindfulness1",
            "",
            "Take some deep breaths. Breathe in through your nose to a count of 4, hold for 1 second and then exhale through the mouth to a count of 5. Repeat often."
        )

        handleStarClick(
            binding.mindfulness2.findViewById(R.id.mindfulnessMed2),
            "mindfulness2",
            "",
            "Enjoy a stroll. As you walk, notice your breath and the sights and sounds around you. As thoughts and worries enter your mind, note them but then return to the present."
        )
        handleStarClick(
            binding.mindfulness3.findViewById(R.id.mindfulnessMed3),
            "mindfulness3",
            "",
            "Practice mindful eating. Be aware of taste, textures, and flavors in each bite, and listen to your body when you are hungry and full."
        )
        handleStarClick(
            binding.mindfulness4.findViewById(R.id.mindfulnessMed4),
            "mindfulness4",
            "",
            "Be aware of your body. Mentally scan your body from head to toe. Bring your attention to how each part feels."
        )
        handleStarClick(
            binding.mindfulness5.findViewById(R.id.mindfulnessMed5),
            "mindfulness5",
            "",
            "Find mindfulness resources,  including online programs and teacher-guided practices."
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
                "mindfulness1" -> binding.mindfulness1.findViewById<ImageButton>(R.id.mindfulnessMed1)
                "mindfulness2" -> binding.mindfulness2.findViewById<ImageButton>(R.id.mindfulnessMed2)
                "mindfulness3" -> binding.mindfulness3.findViewById<ImageButton>(R.id.mindfulnessMed3)
                "mindfulness4" -> binding.mindfulness4.findViewById<ImageButton>(R.id.mindfulnessMed4)
                "mindfulness5" -> binding.mindfulness5.findViewById<ImageButton>(R.id.mindfulnessMed5)
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
