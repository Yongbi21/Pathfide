package com.example.mindpath

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindpath.MainActivity
import com.example.mindpath.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TherapistAccountSetupActivity : AppCompatActivity() {

    private lateinit var profileImageView: CircleImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var birthdateEditText: EditText
    private lateinit var aboutMeEditText: EditText
    private lateinit var educationEditText: EditText
    private lateinit var affiliationEditText: EditText
    private lateinit var clinicalHoursEditText: EditText
    private lateinit var onlineClinicEditText: EditText
    private lateinit var clinicalAddressEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
    private var openingTime: Calendar = Calendar.getInstance()
    private var closingTime: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setup_therapist)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        profileImageView = findViewById(R.id.settingUpdateImage2)
        firstNameEditText = findViewById(R.id.firstNameAccountTherapistProfile)
        lastNameEditText = findViewById(R.id.lastNameAccountTherapistProfile)
        middleNameEditText = findViewById(R.id.middleNameAccountTherapistProfile)
        genderSpinner = findViewById(R.id.genderSpinnerAccountTherapist)
        birthdateEditText = findViewById(R.id.birthdateAccounTherapisttProfile)
        aboutMeEditText = findViewById(R.id.aboutAccounTherapisttProfile)
        educationEditText = findViewById(R.id.educationAccounTherapisttProfile)
        affiliationEditText = findViewById(R.id.affiliateAccounTherapisttProfile)
        clinicalHoursEditText = findViewById(R.id.clinicalHoursTherapistProfile)
        onlineClinicEditText = findViewById(R.id.onlineClinicTherapistProfile)
        clinicalAddressEditText = findViewById(R.id.clinicalAddressTherapistProfile)
        saveButton = findViewById(R.id.saveButton)

        // Set up DatePicker for birthdate field
        birthdateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up TimePicker for clinical hours field
        clinicalHoursEditText.setOnClickListener {
            showTimePickerDialog()
        }

        saveButton.setOnClickListener {
            if (validateInput()) {
                saveTherapistInfo()
            }
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImage(userId: String, onSuccess: (String) -> Unit) {
        val ref = storage.reference.child("profile_images/$userId.jpg")
        val uploadTask = selectedImageUri?.let { ref.putFile(it) }

        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            ref.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Function to show the DatePicker dialog
    private fun showDatePickerDialog() {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Initialize DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Update the birthdate EditText with selected date
                val birthdate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                birthdateEditText.setText(birthdate)
            },
            year, month, day
        )

        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    // Function to show the TimePicker dialog
    private fun showTimePickerDialog() {
        showOpeningTimePicker()
    }

    private fun showOpeningTimePicker() {
        TimePickerDialog(
            this,
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
            this,
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

    private fun validateInput(): Boolean {
        // Add validation logic for inputs (e.g., checking if fields are empty)
        if (firstNameEditText.text.isEmpty() || lastNameEditText.text.isEmpty() ||
            birthdateEditText.text.isEmpty() || clinicalHoursEditText.text.isEmpty())  {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveTherapistInfo() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val therapistInfo = hashMapOf(
                "firstName" to firstNameEditText.text.toString(),
                "lastName" to lastNameEditText.text.toString(),
                "middleName" to middleNameEditText.text.toString(),
                "gender" to genderSpinner.selectedItem.toString(),
                "birthdate" to birthdateEditText.text.toString(),
                "about" to aboutMeEditText.text.toString(),
                "education" to educationEditText.text.toString(),
                "affiliation" to affiliationEditText.text.toString(),
                "clinicalHours" to clinicalHoursEditText.text.toString(),
                "onlineClinic" to onlineClinicEditText.text.toString(),
                "clinicalAddress" to clinicalAddressEditText.text.toString(),
                "userType" to "THERAPIST"
            )

            db.collection("users").document(userId)
                .update(therapistInfo as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Therapist info saved successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USER_TYPE", "THERAPIST")
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving therapist info: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}