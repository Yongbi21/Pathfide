package com.example.pathfide.Fragments.Booking

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfide.Adapter.TherapistAdapter
import com.example.pathfide.Model.Therapist
import com.example.pathfide.databinding.FragmentSeekHelpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SeekHelpFragment : Fragment(), TherapistAdapter.OnItemClickListener {

    private var _binding: FragmentSeekHelpBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var mainAdapter: TherapistAdapter
    private lateinit var searchAdapter: TherapistAdapter
    private var allTherapistsList = mutableListOf<Therapist>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeekHelpBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerViews()
        fetchTherapistData()
        setupSearch()

        return binding.root
    }

    private fun setupRecyclerViews() {
        // Set up the main RecyclerView for all therapists
        itemRecyclerView = binding.itemRecyclerView
        itemRecyclerView.layoutManager = LinearLayoutManager(context)
        mainAdapter = TherapistAdapter(emptyList(), this)
        itemRecyclerView.adapter = mainAdapter

        // Set up the search RecyclerView
        searchRecyclerView = binding.searchRecyclerView
        searchRecyclerView.layoutManager = LinearLayoutManager(context)
        searchAdapter = TherapistAdapter(emptyList(), this)
        searchRecyclerView.adapter = searchAdapter
    }

    override fun onItemClick(therapist: Therapist) {
        val action = SeekHelpFragmentDirections.actionSeekHelpFragmentToProfessionalProfileFragment(
            therapist.fullName,
            therapist.description,
            therapist.location,
            therapist.avatarUrl,
            therapist.education,
            therapist.affiliation,
            therapist.clinicalHours,
            therapist.onlineClinic,
            therapist.physicianRate,
            userId = therapist.userId
        )
        findNavController(requireView()).navigate(action)
    }

    private fun fetchTherapistData() {
        allTherapistsList.clear()

        db.collection("users")
            .whereEqualTo("userType", "THERAPIST")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val firstName = document.getString("firstName") ?: "Mindpath"
                    val lastName = document.getString("surName") ?: "User"
                    val middleName = document.getString("middleName") ?: ""
                    val description = document.getString("about") ?: "No Description"
                    val clinicLocation = document.getString("clinicalAddress") ?: "No Location"
                    val avatarUrl = document.getString("profileImageUrl") ?: ""
                    val education = document.getString("education") ?: ""
                    val affiliation = document.getString("affiliation") ?: ""
                    val clinicalHours = document.getString("clinicTime") ?: ""
                    val onlineClinic = document.getString("onlineClinic") ?: ""
                    val physicianRate = document.getString("physicianRate") ?: ""
                    val fullName = listOf(firstName, middleName, lastName).filter { it.isNotEmpty() }.joinToString(" ")

                    val therapist = Therapist(
                        fullName, description, clinicLocation, avatarUrl, education,
                        affiliation, clinicalHours, onlineClinic, physicianRate, document.id
                    )
                    allTherapistsList.add(therapist)
                }

                // Update the main RecyclerView with all therapists
                mainAdapter.updateData(allTherapistsList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching therapists: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearch() {
        binding.etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTherapists(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterTherapists(query: String) {
        if (query.isEmpty()) {
            // Show the main list and hide search results
            binding.itemRecyclerView.visibility = View.VISIBLE
            binding.searchRecyclerView.visibility = View.GONE
            return
        }

        // Filter therapists whose names contain the query (partial search)
        val filteredList = allTherapistsList.filter { therapist ->
            therapist.fullName.lowercase().contains(query.lowercase())
        }

        // Show search results and hide main list
        binding.itemRecyclerView.visibility = View.GONE
        binding.searchRecyclerView.visibility = View.VISIBLE
        searchAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
