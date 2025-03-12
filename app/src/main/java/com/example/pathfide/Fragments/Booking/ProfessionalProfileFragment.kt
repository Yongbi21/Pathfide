package com.example.pathfide.Fragments.Booking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pathfide.R
import com.example.pathfide.ViewModel.SharedViewModel
import com.example.pathfide.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfessionalProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var fullName: String? = null
    private var description: String? = null
    private var location: String? = null
    private var avatarUrl: String? = null
    private var education: String? = null
    private var affiliation: String? = null
    private var clinicalHours: String? = null
    private var onlineClinic: String? = null
    private var physicianRate: String? = null
    private var userId: String? = null // Change to userId
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            fullName = it.getString("fullName")
            description = it.getString("description")
            location = it.getString("location")
            avatarUrl = it.getString("avatarUrl")
            education = it.getString("education")
            affiliation = it.getString("affiliation")
            clinicalHours = it.getString("clinicalHours")
            onlineClinic = it.getString("onlineClinic")
            physicianRate = it.getString("physicianRate")
            userId = it.getString("userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_professional_profile, container, false)

        // Set up views
        setupViews(view)

        // Set up buttons
        setupButtons(view)

        sharedViewModel.setFullName(fullName ?: "Default Name")
        Log.d("ProfessionalProfileFragment", "Set full name in SharedViewModel: ${fullName ?: "Default Name"}")
        sharedViewModel.setUserId(userId ?: "Default User ID")

        return view
    }

    private fun setupViews(view: View) {
        // Text Views - Add null safety
        view.findViewById<TextView>(R.id.tvFullName)?.text = fullName ?: ""
        view.findViewById<TextView>(R.id.tvAboutMeDescription)?.text = description ?: ""
        view.findViewById<TextView>(R.id.tvLocation)?.text = location ?: ""
        view.findViewById<TextView>(R.id.tvEducationDetails)?.text = education ?: ""
        view.findViewById<TextView>(R.id.tvAffiliationsDetails)?.text = affiliation ?: ""
        view.findViewById<TextView>(R.id.tvAvailableTime)?.text = clinicalHours ?: ""
        view.findViewById<TextView>(R.id.tvOnlineClinic)?.text = onlineClinic ?: ""
        view.findViewById<TextView>(R.id.tvPhysicianRate)?.text = physicianRate ?: "Rate not available"


        // TO DO LATER

        val profileImageView = view.findViewById<ImageView>(R.id.ivAvatar)

        // Only proceed if both ImageView and context are available
        if (profileImageView != null && context != null) {
            try {
                Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)  // Remove duplicate placeholder
                    .circleCrop()
                    .into(profileImageView)
            } catch (e: Exception) {
                // Fallback to default image if Glide loading fails
                profileImageView.setImageResource(R.drawable.person)
            }
        } else {
            // Log the error for debugging
            val errorMessage = when {
                profileImageView == null -> "ImageView is null"
                context == null -> "Context is null"
                else -> "Unknown error"
            }
            android.util.Log.e("ProfessionalProfileFragment", "Image loading failed: $errorMessage")
        }
    }

    private fun setupButtons(view: View) {
        // Chat Button
        view.findViewById<Button>(R.id.messageBtn).setOnClickListener {
            navigateToChat()
        }

        // Appointment Button
        view.findViewById<Button>(R.id.appointmentBtn).setOnClickListener {
            findNavController().navigate(
                ProfessionalProfileFragmentDirections
                    .actionProfessionalProfileFragmentToBookingOptionFragment()
            )
        }
    }

    private fun navigateToChat() {
        userId?.let { partnerId -> // Partner ID is the profile user’s ID
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("ProfessionalProfileFragment", "currentUserId: $currentUserId, partnerId: $partnerId")

            if (!currentUserId.isNullOrEmpty() && !partnerId.isNullOrEmpty()) {
                val chatId = generateChatId(currentUserId, partnerId)
                if (!chatId.isNullOrEmpty()) {
                    // Navigate to ChatFragment with the generated chatId
                    findNavController().navigate(
                        ProfessionalProfileFragmentDirections.actionProfessionalProfileFragmentToChatFragment(chatId)
                    )
                } else {
                    Log.e("ProfessionalProfileFragment", "Failed to generate a valid chat ID")
                }
            } else {
                Log.e("ProfessionalProfileFragment", "One of the IDs is null or empty - currentUserId: $currentUserId, partnerId: $partnerId")
            }
        } ?: run {
            Log.e("ProfessionalProfileFragment", "Profile User ID is null")
        }
    }


    fun generateChatId(userId1: String?, userId2: String?): String? {
        if (userId1.isNullOrEmpty() || userId2.isNullOrEmpty()) {
            Log.e("ChatHelper", "Cannot generate chat ID: userId1=$userId1, userId2=$userId2")
            return null
        }
        return if (userId1 < userId2) "$userId1|$userId2" else "$userId2|$userId1"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
