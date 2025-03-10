package com.example.pathfide.Fragments.Profile

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.pathfide.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.IOException


class TherapistProfileFragment : Fragment(R.layout.fragment_therapist_profile) {

    private lateinit var profileImageView: CircleImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var birthdateEditText: EditText
    private lateinit var aboutEditText: EditText
    private lateinit var educationEditText: EditText
    private lateinit var affiliationEditText: EditText
    private lateinit var clinicalHoursEditText: EditText
    private lateinit var onlineClinicEditText: EditText
    private lateinit var clinicalAddressEditText: EditText
    private lateinit var physicianRateEditText: EditText

    private lateinit var saveButton: Button
    private lateinit var settingsButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null
    private var openingTime: Calendar = Calendar.getInstance()
    private var closingTime: Calendar = Calendar.getInstance()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val TAG = "TherapistProfileFragment"
        private const val UPLOAD_URL = "https://crimsonflare.space/profile_image_handler.php"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI elements
        profileImageView = view.findViewById(R.id.settingUpdateImageTherapist)
        firstNameEditText = view.findViewById(R.id.updateFirstNameAccountTherapistProfile)
        lastNameEditText = view.findViewById(R.id.updateLastNameAccountTherapistProfile)
        middleNameEditText = view.findViewById(R.id.updateMiddleNameAccountTherapistProfile)
        genderSpinner = view.findViewById(R.id.updateGenderSpinnerAccountTherapist)
        birthdateEditText = view.findViewById(R.id.updateBirthdateAccounTherapisttProfile)
        aboutEditText = view.findViewById(R.id.updateAboutAccounTherapisttProfile)
        educationEditText = view.findViewById(R.id.updateEducationAccounTherapisttProfile)
        affiliationEditText = view.findViewById(R.id.updateAffiliateAccounTherapisttProfile)
        clinicalHoursEditText = view.findViewById(R.id.updateClinicalHoursTherapistProfile)
        onlineClinicEditText = view.findViewById(R.id.updateOnlineClinicTherapistProfile)
        clinicalAddressEditText = view.findViewById(R.id.updateClinicalAddressTherapistProfile)
        physicianRateEditText = view.findViewById(R.id.updatephysicianRateTherapistProfile)

        saveButton = view.findViewById(R.id.saveAccountButtonTherapist)
        settingsButton = view.findViewById(R.id.settingsButton)

        // Set up click listeners
        setupClickListeners()

        // Fetch and display user data
        fetchUserData()
    }

    private fun setupClickListeners() {
        profileImageView.setOnClickListener {
            openImageChooser()
        }

        birthdateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        clinicalHoursEditText.setOnClickListener {
            showOpeningTimePicker()
        }

        saveButton.setOnClickListener {
            saveUserData()
        }

        settingsButton.setOnClickListener {
            // TODO: Implement settings functionality
            Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                birthdateEditText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showOpeningTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                openingTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                openingTime.set(Calendar.MINUTE, minute)
                showClosingTimePicker()
            },
            openingTime.get(Calendar.HOUR_OF_DAY),
            openingTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun showClosingTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                closingTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                closingTime.set(Calendar.MINUTE, minute)
                updateClinicalHoursEditText()
            },
            closingTime.get(Calendar.HOUR_OF_DAY),
            closingTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateClinicalHoursEditText() {
        val timeFormat = SimpleDateFormat("h:mma", Locale.getDefault())
        val openingTimeString = timeFormat.format(openingTime.time).toLowerCase(Locale.getDefault())
        val closingTimeString = timeFormat.format(closingTime.time).toLowerCase(Locale.getDefault())
        clinicalHoursEditText.setText("$openingTimeString - $closingTimeString")
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Retain previous functionality while focusing on specific fields
                        firstNameEditText.setText(document.getString("firstName"))
                        lastNameEditText.setText(document.getString("lastName"))
                        middleNameEditText.setText(document.getString("middleName"))

                        // Fetch and set additional relevant data
                        val aboutMe = document.getString("about") ?: ""
                        val clinicalAddress = document.getString("clinicalAddress") ?: ""

                        aboutEditText.setText(aboutMe)
                        clinicalAddressEditText.setText(clinicalAddress)

                        // Retain other fields but keep their functionality
                        birthdateEditText.setText(document.getString("birthdate"))
                        educationEditText.setText(document.getString("education"))
                        affiliationEditText.setText(document.getString("affiliation"))
                        clinicalHoursEditText.setText(document.getString("clinicalHours"))
                        onlineClinicEditText.setText(document.getString("onlineClinic"))
                        val physicianRate = document.getString("physicianRate") ?: "0.00"
                        val displayRate = if (physicianRate.startsWith("PHP ")) physicianRate else "PHP $physicianRate"
                        physicianRateEditText.setText(displayRate)
                        // Set gender spinner selection
                        val genderArray = resources.getStringArray(R.array.gender_array)
                        val genderPosition = genderArray.indexOf(document.getString("gender"))
                        if (genderPosition != -1) {
                            genderSpinner.setSelection(genderPosition)
                        }
                    }
                    val imageUrl = document.getString("profileImageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(profileImageView)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid

        var physicianRateText = physicianRateEditText.text.toString().trim()

        // Strip "PHP " if it exists before saving
        if (physicianRateText.startsWith("PHP ")) {
            physicianRateText = physicianRateText.substring(4).trim()
        }

        // Add "PHP " only if it doesn't already start with it
        if (!physicianRateText.startsWith("PHP ")) {
            physicianRateText = "PHP $physicianRateText"
        }

        // Validate that physicianRateText is not "0" or empty
        val physicianRateValue = physicianRateText.replace("PHP", "").trim()
        if (physicianRateValue.isEmpty() || physicianRateValue == "0") {
            // Show an error message or toast to the user
            Toast.makeText(context, "Physician rate cannot be 0 or empty.", Toast.LENGTH_SHORT).show()
            return // Exit the function if validation fails
        }


        if (userId != null) {
            if (!validateNames()) {
                Toast.makeText(context, "Please correct the name fields", Toast.LENGTH_SHORT).show()
                return
            }
            val userInfo = hashMapOf(
                "firstName" to firstNameEditText.text.toString(),
                "lastName" to lastNameEditText.text.toString(),
                "middleName" to middleNameEditText.text.toString(),
                "gender" to genderSpinner.selectedItem.toString(),
                "birthdate" to birthdateEditText.text.toString(),
                "about" to aboutEditText.text.toString(),
                "education" to educationEditText.text.toString(),
                "affiliation" to affiliationEditText.text.toString(),
                "clinicalHours" to clinicalHoursEditText.text.toString(),
                "onlineClinic" to onlineClinicEditText.text.toString(),
                "clinicalAddress" to clinicalAddressEditText.text.toString(),
                "physicianRate" to physicianRateText
            )

            if (selectedImageUri != null) {
                uploadImageToServer(userId) { imageUrl ->
                    userInfo["profileImageUrl"] = imageUrl
                    updateUserData(userId, userInfo)
                }
            } else {
                updateUserData(userId, userInfo)
            }


        }
    }



    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileExtension = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
            val tempFile = File.createTempFile("upload", ".$fileExtension", context.cacheDir)
            tempFile.outputStream().use { output -> inputStream.copyTo(output) }
            tempFile
        } catch (e: Exception) {
            null
        }
    }


    private fun uploadImageToServer(userId: String, onSuccess: (String) -> Unit) {
        if (selectedImageUri == null) return

        val file = getFileFromUri(requireContext(), selectedImageUri!!)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Failed to prepare image for upload", Toast.LENGTH_SHORT).show()
            return
        }

        val mimeType = requireContext().contentResolver.getType(selectedImageUri!!)
        if (mimeType != "image/jpeg" && mimeType != "image/png") {
            Toast.makeText(context, "Invalid file type. Only JPG and PNG allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val mediaType = mimeType.toMediaType()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("profile_image", file.name, RequestBody.create(mediaType, file))
            .build()

        val request = Request.Builder().url(UPLOAD_URL).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    try {
                        val responseJson = JSONObject(responseBody ?: "")
                        val imageUrl = responseJson.optString("image_url")
                        activity?.runOnUiThread {
                            onSuccess(imageUrl)
                        }
                    } catch (e: Exception) {
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Error parsing server response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Image upload failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateUserData(userId: String, userInfo: HashMap<String, String>) {
        db.collection("users").document(userId)
            .update(userInfo as Map<String, Any>)
            .addOnSuccessListener {
                fetchUserData()
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isValidName(name: String): Boolean {
        // Regex pattern that allows only letters (including accented characters) and spaces
        val namePattern = "^[\\p{L}\\s]+$".toRegex()
        return name.isNotEmpty() && namePattern.matches(name)
    }

    private fun validateNames(): Boolean {
        var isValid = true

        // Validate first name
        if (!isValidName(firstNameEditText.text.toString().trim())) {
            firstNameEditText.error = "First name should only contain letters"
            isValid = false
        }

        // Validate last name
        if (!isValidName(lastNameEditText.text.toString().trim())) {
            lastNameEditText.error = "Last name should only contain letters"
            isValid = false
        }

        // Validate middle name if not empty
        val middleName = middleNameEditText.text.toString().trim()
        if (middleName.isNotEmpty() && !isValidName(middleName)) {
            middleNameEditText.error = "Middle name should only contain letters"
            isValid = false
        }

        return isValid
    }

}