package com.example.pathfide.Fragments.Profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.pathfide.R
import com.example.pathfide.ViewModel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class ClientProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var profileImageView: CircleImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var birthdateEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postViewModel: PostViewModel

    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val TAG = "ClientProfileFragment"
        private const val UPLOAD_URL = "https://crimsonflare.space/profile_image_handler.php"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated called")

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        profileImageView = view.findViewById(R.id.settingUpdateImageClient)
        firstNameEditText = view.findViewById(R.id.firstNameProfile)
        lastNameEditText = view.findViewById(R.id.lastNameProfile)
        middleNameEditText = view.findViewById(R.id.middleNameProfile)
        genderSpinner = view.findViewById(R.id.genderSpinner2)
        birthdateEditText = view.findViewById(R.id.birthdateProfile)
        numberEditText = view.findViewById(R.id.updateNumberProfile)
        saveButton = view.findViewById(R.id.saveClientButton)

        // Set up click listeners
        setupClickListeners()

        // Fetch and display user data
        fetchUserData()

        postViewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]

    }

    private fun setupClickListeners() {
        profileImageView.setOnClickListener {
            Log.d(TAG, "Profile image clicked")
            openImageChooser()
        }

        birthdateEditText.setOnClickListener {
            Log.d(TAG, "Birthdate edit text clicked")
            showDatePickerDialog()
        }

        saveButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            saveUserData()
        }
    }

    private fun openImageChooser() {
        Log.d(TAG, "Opening image chooser")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode")

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Log.d(TAG, "Image selected: $selectedImageUri")
            profileImageView.setImageURI(selectedImageUri)
        } else {
            Log.e(TAG, "Image selection failed or cancelled")
        }
    }

    private fun showDatePickerDialog() {
        Log.d(TAG, "Showing date picker dialog")
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                birthdateEditText.setText(selectedDate)
                Log.d(TAG, "Selected birthdate: $selectedDate")
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        Log.d(TAG, "Fetching user data for userId: $userId")

        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        firstNameEditText.setText(document.getString("firstName"))
                        lastNameEditText.setText(document.getString("lastName"))
                        middleNameEditText.setText(document.getString("middleName"))
                        birthdateEditText.setText(document.getString("birthdate"))
                        numberEditText.setText(document.getString("contactNumber"))

                        val genderArray = resources.getStringArray(R.array.gender_array)
                        val genderPosition = genderArray.indexOf(document.getString("gender"))
                        if (genderPosition != -1) {
                            genderSpinner.setSelection(genderPosition)
                        }

                        val imageUrl = document.getString("profileImageUrl")
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(imageUrl).placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile).into(profileImageView)
                        }
                    } else {
                        Toast.makeText(context, "No such user document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Perform validation before saving
            if (!validateNames()) {
                Toast.makeText(context, "Please correct the name fields", Toast.LENGTH_SHORT).show()
                return
            }

            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val middleName = middleNameEditText.text.toString().trim()

            val userInfo = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "middleName" to middleName,
                "gender" to genderSpinner.selectedItem.toString(),
                "birthdate" to birthdateEditText.text.toString(),
                "contactNumber" to numberEditText.text.toString()
            )

            if (selectedImageUri != null) {
                uploadImageToServer(userId) { imageUrl ->
                    userInfo["profileImageUrl"] = imageUrl
                    updateUserData(userId, userInfo)
                    updateUserPostsData(firstName, lastName, imageUrl)
                    updateUserCommentsData(userId, firstName, lastName, imageUrl)
                }
            } else {
                updateUserData(userId, userInfo)
                updateUserPostsData(firstName, lastName, null)
                updateUserCommentsData(userId, firstName, lastName, null)
            }
        }
    }

    private fun updateUserPostsData(firstName: String, lastName: String, profileImageUrl: String?) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val postsRef = db.collection("posts")
            postsRef.whereEqualTo("userId", userId).get()
                .addOnSuccessListener { snapshot ->
                    val batch = db.batch()
                    for (document in snapshot.documents) {
                        // Update firstName and lastName
                        batch.update(document.reference, "firstName", firstName)
                        batch.update(document.reference, "lastName", lastName)

                        // Update profileImageUrl if not null
                        if (profileImageUrl != null) {
                            batch.update(document.reference, "profileImageUrl", profileImageUrl)
                        }
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully updated user data in posts.")
                            // Use a broadcast to notify that profile has been updated
                            val intent = Intent("com.example.pathfide.PROFILE_UPDATED")
                            context?.sendBroadcast(intent)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update user data in posts: ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to retrieve user posts: ${e.message}", e)
                }
        }
    }

    private fun updateUserCommentsData(userId: String, firstName: String, lastName: String, profileImageUrl: String?) {
        db.collectionGroup("comments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (document in snapshot.documents) {
                    // Update firstName and lastName
                    batch.update(document.reference, "firstName", firstName)
                    batch.update(document.reference, "lastName", lastName)

                    // Update profileImageUrl if not null
                    if (profileImageUrl != null) {
                        batch.update(document.reference, "profileImageUrl", profileImageUrl)
                    }
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully updated user data in ${snapshot.size()} comments.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update user data in comments: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to retrieve user comments: ${e.message}", e)
            }
    }


    private fun uploadImageToServer(userId: String, onSuccess: (String) -> Unit) {
        if (selectedImageUri == null) {
            Log.e(TAG, "No image URI selected for upload")
            return
        }

        val file = getFileFromUri(requireContext(), selectedImageUri!!)
        if (file == null || !file.exists()) {
            Log.e(TAG, "Failed to convert URI to file")
            Toast.makeText(context, "Failed to prepare image for upload", Toast.LENGTH_SHORT).show()
            return
        }

        // Check file MIME type (only allow JPG and PNG)
        val mimeType = requireContext().contentResolver.getType(selectedImageUri!!)
        if (mimeType != "image/jpeg" && mimeType != "image/png") {
            Log.e(TAG, "Invalid file type. Only JPG and PNG allowed.")
            Toast.makeText(context, "Invalid file type. Only JPG and PNG allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val client = OkHttpClient()

            val mediaType = mimeType?.toMediaType() ?: "image/*".toMediaType()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart(
                    "profile_image",
                    file.name,
                    RequestBody.create(mediaType, file)
                )
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            Log.d(TAG, "Uploading image to $UPLOAD_URL with file: ${file.name}")

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Image upload failed: ${e.message}", e)
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Image upload response: $responseBody")

                    if (response.isSuccessful) {
                        try {
                            val responseJson = JSONObject(responseBody ?: "")
                            val imageUrl = responseJson.optString("image_url")
                            Log.d(TAG, "Image upload successful, URL: $imageUrl")
                            activity?.runOnUiThread {
                                onSuccess(imageUrl)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing image upload response: ${e.message}", e)
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Error parsing server response", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e(TAG, "Image upload failed: HTTP ${response.code} - ${response.message}")
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Image upload failed: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing image for upload: ${e.message}", e)
            activity?.runOnUiThread {
                Toast.makeText(context, "Error preparing image for upload: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileExtension = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
            val tempFile = File.createTempFile("upload", ".$fileExtension", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to File: ${e.message}", e)
            null
        }
    }


    private fun updateUserData(userId: String, userInfo: HashMap<String, String>) {
        db.collection("users").document(userId)
            .update(userInfo as Map<String, Any>)
            .addOnSuccessListener {
                fetchUserData() // Refresh the data to update the UI
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateUserPostsProfileImage(profileImageUrl: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val postsRef = db.collection("posts")
            postsRef.whereEqualTo("userId", userId).get()
                .addOnSuccessListener { snapshot ->
                    val batch = db.batch()
                    for (document in snapshot.documents) {
                        batch.update(document.reference, "profileImageUrl", profileImageUrl)
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully updated profile image in user posts.")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update profile image in posts: ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to retrieve user posts: ${e.message}", e)
                }
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

