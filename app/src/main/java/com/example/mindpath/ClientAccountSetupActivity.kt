        package com.example.mindpath

        import android.annotation.SuppressLint
        import android.app.Activity
        import android.app.DatePickerDialog
        import android.content.Intent
        import android.graphics.Bitmap
        import android.graphics.BitmapFactory
        import android.graphics.PointF
        import android.net.Uri
        import android.os.Bundle
        import android.provider.OpenableColumns
        import android.util.Log
        import android.view.View
        import android.widget.Button
        import android.widget.EditText
        import android.widget.ImageView
        import android.widget.LinearLayout
        import android.widget.Spinner
        import android.widget.Toast
        import androidx.appcompat.app.AppCompatActivity
        import com.example.mindpath.MainActivity
        import com.example.mindpath.R
        import com.google.firebase.auth.FirebaseAuth
        import com.google.firebase.firestore.FirebaseFirestore
        import com.google.firebase.storage.FirebaseStorage
        import com.google.i18n.phonenumbers.PhoneNumberUtil
        import com.google.i18n.phonenumbers.Phonenumber
        import okhttp3.Call
        import okhttp3.Callback
        import okhttp3.MediaType.Companion.toMediaTypeOrNull
        import okhttp3.MultipartBody
        import okhttp3.OkHttpClient
        import okhttp3.Request
        import okhttp3.RequestBody.Companion.asRequestBody
        import okhttp3.Response
        import okio.IOException
        import java.io.File
        import java.io.FileOutputStream
        import java.util.*
        import com.google.mlkit.vision.text.TextRecognition
        import com.google.mlkit.vision.common.InputImage
        import com.google.mlkit.vision.text.latin.TextRecognizerOptions
        import com.google.mlkit.vision.face.FaceDetectorOptions
        import com.google.mlkit.vision.face.FaceLandmark
        import com.google.mlkit.vision.face.FaceDetection
        import android.Manifest
        import android.content.pm.PackageManager
        import androidx.core.app.ActivityCompat
        import androidx.core.content.ContextCompat


        class ClientAccountSetupActivity : AppCompatActivity() {

            private lateinit var firstNameEditText: EditText
            private lateinit var lastNameEditText: EditText
            private lateinit var middleNameEditText: EditText
            private lateinit var genderSpinner: Spinner
            private lateinit var birthdateEditText: EditText
            private lateinit var contactNumberEditText: EditText
            private lateinit var uploadValidIdEditText: EditText
            private lateinit var selfieImageView: ImageView
            private lateinit var openCameraButton: Button
            private lateinit var selfieSection: LinearLayout
            private lateinit var saveButton: Button
            private lateinit var auth: FirebaseAuth
            private lateinit var db: FirebaseFirestore
            private lateinit var storage: FirebaseStorage

            private var selectedIdUri: Uri? = null
            private var idUrl: String? = null
            private var idFaceLandmarks: Array<FaceLandmark?> = arrayOfNulls(3)
            private val CAMERA_PERMISSION_REQUEST_CODE = 1001

            companion object {
                private const val REQUEST_CODE_PICK_ID = 100
                private const val REQUEST_CODE_TAKE_SELFIE = 101
            }

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_account_setup_client)

                // Initialize Firebase instances
                auth = FirebaseAuth.getInstance()
                db = FirebaseFirestore.getInstance()
                storage = FirebaseStorage.getInstance()

                firstNameEditText = findViewById(R.id.firstNameAccountProfile)
                lastNameEditText = findViewById(R.id.lastNameAccountProfile)
                middleNameEditText = findViewById(R.id.middleNameAccountProfile)
                genderSpinner = findViewById(R.id.genderSpinnerAccount)
                birthdateEditText = findViewById(R.id.birthdateAccountProfile)
                contactNumberEditText = findViewById(R.id.numberAccountProfile)
                saveButton = findViewById(R.id.saveAccountButton)
                selfieImageView = findViewById(R.id.selfieImageView)
                openCameraButton = findViewById(R.id.openCameraButton)
                selfieSection = findViewById(R.id.selfieSection)
                uploadValidIdEditText = findViewById(R.id.uploadValidId)


                // Set save button to be visible by default
                saveButton.visibility = View.GONE

                // Hide the selfie section initially
                selfieSection.visibility = View.GONE

                // Set up DatePicker for birthdate field
                birthdateEditText.setOnClickListener { showDatePickerDialog() }

                // Set up click listener for uploading valid ID
                uploadValidIdEditText.setOnClickListener { openFilePicker() }

                // Set up camera button
                openCameraButton.setOnClickListener {
                    openCamera()
                }

                saveButton.setOnClickListener {
                    if (validateInput()) {
                        saveClientInfo()
                    }
                }
            }

            private fun openFilePicker() {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "Select ID"), REQUEST_CODE_PICK_ID)
            }

            private fun openCamera() {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                } else {
                    launchCameraIntent()
                }
            }

            private fun launchCameraIntent() {
                val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_SELFIE)
                } else {
                    Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
                    if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        launchCameraIntent()
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }



            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                super.onActivityResult(requestCode, resultCode, data)
                if (requestCode == REQUEST_CODE_PICK_ID && resultCode == Activity.RESULT_OK && data != null) {
                    selectedIdUri = data.data
                    selectedIdUri?.let { uri ->
                        val fileName = getFileName(uri)
                        uploadValidIdEditText.setText(fileName)

                        // Process the ID image for validation
                        processIdImage(uri)

                        // Show selfie section after a valid ID is selected
                        if (selectedIdUri != null) {
                            selfieSection.visibility = View.VISIBLE
                            saveButton.visibility = View.GONE
                        }
                    }
                }

                if (requestCode == REQUEST_CODE_TAKE_SELFIE && resultCode == Activity.RESULT_OK && data != null) {
                    val imageBitmap = data.extras?.get("data") as Bitmap
                    selfieImageView.setImageBitmap(imageBitmap)

                    // Upload the selfie (optional)
                    uploadSelfie(imageBitmap)

                    // Process the selfie image
                    processSelfieImage(imageBitmap)

                    // Show the save button after the selfie is taken
                    saveButton.visibility = View.VISIBLE
                }
            }



            @SuppressLint("Range")
            private fun getFileName(uri: Uri): String {
                var name = "Unknown"
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
                return name
            }

            private fun showDatePickerDialog() {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    birthdateEditText.setText(selectedDate)
                }, year, month, day)

                datePickerDialog.show()
            }

            private fun validateInput(): Boolean {
                if (firstNameEditText.text.isEmpty() || lastNameEditText.text.isEmpty() ||
                    birthdateEditText.text.isEmpty() || contactNumberEditText.text.isEmpty()) {
                    Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                    return false
                }

                // Validate the phone number
                return validatePhoneNumber(contactNumberEditText.text.toString())
            }

            private fun validatePhoneNumber(phoneNumber: String): Boolean {
                val PH_COUNTRY_CODE = "+63"
                return try {
                    val sanitizedNumber = sanitizePhoneNumber(phoneNumber)
                    if (sanitizedNumber.length < 10) {
                        Toast.makeText(this, "Philippine phone number must have 10 digits", Toast.LENGTH_SHORT).show()
                        return false
                    }

                    val number: Phonenumber.PhoneNumber = PhoneNumberUtil.getInstance().parse(sanitizedNumber, "PH")
                    if (PhoneNumberUtil.getInstance().isValidNumber(number)) {
                        true
                    } else {
                        Toast.makeText(this, "Invalid Philippine phone number", Toast.LENGTH_SHORT).show()
                        false
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid phone number: ${e.message}", Toast.LENGTH_SHORT).show()
                    false
                }
            }

            private fun sanitizePhoneNumber(phoneNumber: String): String {
                return if (phoneNumber.startsWith("+63")) {
                    phoneNumber.replace(Regex("[^\\d]"), "").drop(2)
                } else {
                    phoneNumber.replace(Regex("[^\\d]"), "")
                }
            }

            private fun saveClientInfo() {

                if (!isValidInput()) {
                    Toast.makeText(this, "Please complete all fields and ensure they are valid.", Toast.LENGTH_SHORT).show()
                    return
                }

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    if (selectedIdUri != null) {
                        uploadIdImage(userId) { imageUrl ->
                            idUrl = imageUrl
                            saveClientDataToFirestore(userId)
                        }
                    } else {
                        saveClientDataToFirestore(userId)
                    }
                }
            }

            private fun uploadIdImage(userId: String, onSuccess: (String) -> Unit) {
                if (selectedIdUri == null) return

                val file = getFileFromUri(selectedIdUri!!)
                file?.let {
                    uploadImageToServer(file, userId, "id") { imageUrl ->
                        idUrl = imageUrl
                        onSuccess(imageUrl)
                    }
                }
            }

            private fun processIdImage(uri: Uri) {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                // Detect faces in the ID image
                detectFaceInIdImage(bitmap)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        Log.d("ClientAccountSetupActivity", "Extracted Text: $extractedText")

                        val isValidText = isValidIdText(extractedText)
                        val isValidDimensions = isImageWithinExpectedDimensions(uri)

                        Log.d("ClientAccountSetupActivity", "Is Valid ID Text: $isValidText")
                        Log.d("ClientAccountSetupActivity", "Is Valid Image Dimensions: $isValidDimensions")

                        if (isValidText && isValidDimensions) {
                            Toast.makeText(this, "Valid ID detected", Toast.LENGTH_SHORT).show()
                            autoFillFieldsFromText(extractedText)

                            // Show selfie section for valid ID
                            selfieSection.visibility = View.VISIBLE
                            toggleSaveButtonVisibility()
                        } else {
                            handleInvalidIdFeedback(extractedText) // Provide feedback if ID is invalid
                            selectedIdUri = null // Reset invalid selection
                            selfieSection.visibility = View.GONE
                            toggleSaveButtonVisibility()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to process ID image.", Toast.LENGTH_SHORT).show()
                        selectedIdUri = null
                        selfieSection.visibility = View.GONE
                        toggleSaveButtonVisibility()
                    }
            }

            private fun detectFaceInIdImage(bitmap: Bitmap) {
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                val options = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build()

                val faceDetector = FaceDetection.getClient(options)

                faceDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val face = faces[0]
                            val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                            val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                            val nose = face.getLandmark(FaceLandmark.NOSE_BASE)
                            val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)
                            val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)

                            // Essential landmarks check
                            if (leftEye == null || rightEye == null || nose == null || mouthLeft == null || mouthRight == null) {
                                Log.e("FaceComparison", "Essential landmarks are missing in ID image.")
                                return@addOnSuccessListener
                            }

                            idFaceLandmarks = arrayOf(leftEye, rightEye, nose, mouthLeft, mouthRight)
                            Log.d("FaceDetection", "ID Face Landmarks: $leftEye, $rightEye, $nose, $mouthLeft, $mouthRight")
                        } else {
                            Log.e("FaceDetection", "No faces detected in ID image.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FaceDetection", "Failed to detect face in ID: ${e.message}")
                    }
            }

            private fun processSelfieImage(bitmap: Bitmap) {
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                val options = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build()

                val faceDetector = FaceDetection.getClient(options)

                faceDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val face = faces[0]
                            val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                            val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                            val nose = face.getLandmark(FaceLandmark.NOSE_BASE)
                            val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)
                            val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)

                            // Essential landmarks check
                            if (leftEye == null || rightEye == null || nose == null || mouthLeft == null || mouthRight == null) {
                                Log.e("FaceComparison", "Essential landmarks are missing in selfie.")
                                return@addOnSuccessListener
                            }

                            compareFaces(idFaceLandmarks, arrayOf(leftEye, rightEye, nose, mouthLeft, mouthRight))
                        } else {
                            Log.e("FaceDetection", "No faces detected in selfie image.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FaceDetection", "Failed to detect face in selfie: ${e.message}")
                    }
            }

            private fun compareFaces(
                idLandmarks: Array<FaceLandmark?>,
                selfieLandmarks: Array<FaceLandmark?>
            ) {
                if (idLandmarks.size != selfieLandmarks.size) {
                    Log.e("FaceComparison", "Landmarks don't match in number.")
                    return
                }

                val normalizedIdLandmarks = normalizeLandmarks(idLandmarks)
                val normalizedSelfieLandmarks = normalizeLandmarks(selfieLandmarks)

                if (normalizedIdLandmarks.any { it == null } || normalizedSelfieLandmarks.any { it == null }) {
                    Log.e("FaceComparison", "Normalization resulted in null landmarks.")
                    return
                }

                // Dynamically adjusted weights
                val weights = listOf(0.25, 0.25, 0.2, 0.15, 0.15) // Adjust weights as necessary
                var totalDistance = 0.0

                for (i in normalizedIdLandmarks.indices) {
                    val idLandmark = normalizedIdLandmarks[i]
                    val selfieLandmark = normalizedSelfieLandmarks[i]

                    if (idLandmark != null && selfieLandmark != null) {
                        val distance = calculateDistance(idLandmark, selfieLandmark)
                        val weightedDistance = distance * weights[i]
                        totalDistance += weightedDistance
                        Log.d("FaceComparison", "Weighted distance for landmark $i: $weightedDistance")
                    } else {
                        Log.e("FaceComparison", "Null landmark detected at index $i")
                    }
                }

                // Dynamic threshold calculation based on weights and landmarks
                val threshold = calculateDynamicThreshold(totalDistance, normalizedIdLandmarks.size)
                Log.d("FaceComparison", "Total Distance: $totalDistance, Threshold: $threshold")

                if (totalDistance < threshold) {
                    Toast.makeText(this, "Verification process saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Verification process failed.", Toast.LENGTH_SHORT).show()
                }
            }

            private fun calculateDistance(point1: PointF, point2: PointF): Double {
                val dx = point2.x - point1.x
                val dy = point2.y - point1.y
                return Math.sqrt(dx * dx + dy * dy.toDouble())
            }

            private fun normalizeLandmarks(landmarks: Array<FaceLandmark?>): Array<PointF?> {
                val normalizedLandmarks = Array<PointF?>(landmarks.size) { null }
                var centerX = 0f
                var centerY = 0f
                var maxDistance = 0f
                var validLandmarkCount = 0

                // Calculate the center of the landmarks
                for (landmark in landmarks) {
                    if (landmark != null) {
                        centerX += landmark.position.x
                        centerY += landmark.position.y
                        validLandmarkCount++
                    }
                }

                if (validLandmarkCount == 0) {
                    Log.e("FaceComparison", "No valid landmarks for normalization.")
                    return normalizedLandmarks
                }

                centerX /= validLandmarkCount
                centerY /= validLandmarkCount

                // Calculate the maximum distance between landmarks for scaling
                for (landmark in landmarks) {
                    if (landmark != null) {
                        val dx = landmark.position.x - centerX
                        val dy = landmark.position.y - centerY
                        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        maxDistance = maxOf(maxDistance, distance)
                    }
                }

                if (maxDistance == 0f) {
                    Log.e("FaceComparison", "Maximum distance is zero, unable to normalize landmarks.")
                    return normalizedLandmarks
                }

                // Normalize the landmarks relative to the center and scale
                for (i in landmarks.indices) {
                    val landmark = landmarks[i]
                    if (landmark != null) {
                        val scaledX = (landmark.position.x - centerX) / maxDistance
                        val scaledY = (landmark.position.y - centerY) / maxDistance
                        normalizedLandmarks[i] = PointF(scaledX, scaledY)
                    }
                }

                return normalizedLandmarks
            }

            private fun calculateDynamicThreshold(totalDistance: Double, landmarkCount: Int): Double {
                val baseThreshold = totalDistance / landmarkCount
                val safetyFactor = 1.5 // Slightly lenient
                val minimumThreshold = 0.3 // Ensure realistic matches
                return maxOf(baseThreshold * safetyFactor, minimumThreshold)
            }




            private fun isValidIdText(text: String): Boolean {
                // Patterns for different ID types

                // National ID pattern (e.g., "PHL123456789")
                val nationalIdRegex = Regex("\\d{4}-\\d{4}-\\d{4}-\\d{4}")

                // Passport number pattern (e.g., "A12345678")
                val passportRegex = Regex("[A-Z]{1,2}\\d{6,8}[A-Z]")

                // School ID pattern (adjust as needed based on school standards)
                val schoolIdRegex = Regex("\\d{4}-\\d{5}") // Example: "2020-01294"

                // Philippine Driver's License pattern
                val driversLicenseRegex = Regex("[A-Z]\\d{2}-\\d{2}-\\d{6}")

                // Name pattern (for matching names in IDs)
                val nameRegex = Regex("([A-Za-z]+(?: [A-Za-z]+)*)") // This can handle multiple name parts

                // Check for valid ID type matches
                val containsNationalId = nationalIdRegex.containsMatchIn(text)
                val containsPassport = passportRegex.containsMatchIn(text)
                val containsSchoolId = schoolIdRegex.containsMatchIn(text)
                val containsDriversLicense = driversLicenseRegex.containsMatchIn(text)

                // Check if text contains at least one of the valid ID types
                val isValidId = containsNationalId || containsPassport || containsDriversLicense || containsSchoolId

                // For valid IDs, we can optionally check if there is a name (e.g., if it's a valid ID document)
                val containsValidName = nameRegex.containsMatchIn(text)

                // Log for debugging
                Log.d("ClientAccountSetupActivity", "Text: $text")
                Log.d("ClientAccountSetupActivity", "Contains National ID: $containsNationalId")
                Log.d("ClientAccountSetupActivity", "Contains Passport: $containsPassport")
                Log.d("ClientAccountSetupActivity", "Contains School ID: $containsSchoolId")
                Log.d("ClientAccountSetupActivity", "Contains Driver's License: $containsDriversLicense")
                Log.d("ClientAccountSetupActivity", "Contains Valid Name: $containsValidName")

                // If text contains any valid ID type (not just name), it's valid
                return isValidId
            }



            private fun isImageWithinExpectedDimensions(uri: Uri): Boolean {
                return try {
                    val inputStream = contentResolver.openInputStream(uri) ?: return false
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream.close()

                    val width = options.outWidth
                    val height = options.outHeight

                    // Log the dimensions for debugging purposes
                    Log.d("ClientAccountSetupActivity", "Image Dimensions: Width = $width, Height = $height")

                    // Relaxed checks: Allow any width and height greater than 200px
                    val isLargeEnough = width > 200 && height > 200

                    // Allow a much broader aspect ratio range (from 0.3 to 3.0, for example)
                    val isAspectRatioAcceptable = (width.toFloat() / height.toFloat() in 0.3..3.0)

                    isLargeEnough && isAspectRatioAcceptable
                } catch (e: Exception) {
                    Toast.makeText(this, "Error checking ID dimensions: ${e.message}", Toast.LENGTH_SHORT).show()
                    false
                }
            }


            private fun autoFillFieldsFromText(text: String) {
                val nameRegex = Regex("([A-Z][a-z]+) ([A-Z][a-z]+) ([A-Z]\\.)?|([A-Z\\s]+), ([A-Z][a-z]+) ([A-Z]\\.)?")
                val dateRegex = Regex("\\d{2}/\\d{2}/\\d{4}")

                val nameMatch = nameRegex.find(text)?.value ?: ""
                val dateMatch = dateRegex.find(text)?.value ?: ""

                if (nameMatch.isNotEmpty()) {
                    // Split the name and handle formatting based on match patterns
                    val nameParts = nameMatch.split(", ", " ")
                    if (nameParts.isNotEmpty()) {
                        when (nameParts.size) {
                            2 -> {
                                firstNameEditText.setText(formatName(nameParts[1]))
                                middleNameEditText.setText(formatName(nameParts[2]))
                                lastNameEditText.setText(formatName(nameParts[0]))
                            }
                            3 -> {
                                firstNameEditText.setText(formatName(nameParts[1]))
                                lastNameEditText.setText(formatName(nameParts[0]))
                                middleNameEditText.setText(formatName(nameParts[2]))
                            }
                        }
                    }
                }

                if (dateMatch.isNotEmpty()) {
                    birthdateEditText.setText(dateMatch)
                }
            }

            private fun formatName(name: String): String {
                return if (name.all { it.isUpperCase() }) {
                    name.lowercase().replaceFirstChar { it.uppercase() }
                } else {
                    name
                }
            }


            private fun handleInvalidIdFeedback(text: String) {
                // Patterns for name and ID types
                val nameRegex = Regex("([A-Za-z]+(?: [A-Za-z]+)*)")   // Name pattern
                val nationalIdRegex = Regex("\\d{4}-\\d{4}-\\d{4}-\\d{4}")               // National ID pattern (PHL followed by 9 digits)
                val passportRegex = Regex("[A-Z]{1,2}\\d{6,8}[A-Z]")
                val schoolIdRegex = Regex("\\d{4}-\\d{5}")             // School ID pattern (4 digits hyphen 5 digits)
                val driversLicenseRegex = Regex("DL\\d{10}")           // Driver's License pattern (DL followed by 10 digits)

                // Check if the text contains a valid name and ID
                val containsName = nameRegex.containsMatchIn(text)
                val containsNationalId = nationalIdRegex.containsMatchIn(text)
                val containsPassport = passportRegex.containsMatchIn(text)
                val containsSchoolId = schoolIdRegex.containsMatchIn(text)
                val containsDriversLicense = driversLicenseRegex.containsMatchIn(text)

                // Log matches for debugging
                Log.d("ClientAccountSetupActivity", "Processing ID text: $text")
                Log.d("ClientAccountSetupActivity", "Contains valid name: $containsName")
                Log.d("ClientAccountSetupActivity", "Contains valid National ID: $containsNationalId")
                Log.d("ClientAccountSetupActivity", "Contains valid Passport: $containsPassport")
                Log.d("ClientAccountSetupActivity", "Contains valid School ID: $containsSchoolId")
                Log.d("ClientAccountSetupActivity", "Contains valid Driver's License: $containsDriversLicense")

                // Check if the text contains a valid ID before proceeding
                if (containsName) {
                    if (containsNationalId || containsPassport || containsSchoolId || containsDriversLicense) {
                        // Feedback for valid ID types
                        when {
                            containsNationalId -> {
                                Toast.makeText(this, "Valid National ID detected.", Toast.LENGTH_SHORT).show()
                                Log.d("ClientAccountSetupActivity", "Valid National ID.")
                            }
                            containsPassport -> {
                                Toast.makeText(this, "Valid Passport detected.", Toast.LENGTH_SHORT).show()
                                Log.d("ClientAccountSetupActivity", "Valid Passport.")
                            }
                            containsSchoolId -> {
                                Toast.makeText(this, "Valid School ID detected.", Toast.LENGTH_SHORT).show()
                                Log.d("ClientAccountSetupActivity", "Valid School ID.")
                            }
                            containsDriversLicense -> {
                                Toast.makeText(this, "Valid Driver's License detected.", Toast.LENGTH_SHORT).show()
                                Log.d("ClientAccountSetupActivity", "Valid Driver's License.")
                            }
                        }
                    } else {
                        // If no valid ID type is found
                        Toast.makeText(this, "ID is invalid. Ensure it contains a valid name and ID format.", Toast.LENGTH_SHORT).show()
                        Log.e("ClientAccountSetupActivity", "Invalid ID: Missing name or valid ID format.")
                    }
                } else {
                    // If no valid name is found
                    Toast.makeText(this, "ID is missing a valid name format. Please check.", Toast.LENGTH_SHORT).show()
                    Log.e("ClientAccountSetupActivity", "Invalid ID: Missing valid name format.")
                }
            }


            private fun saveClientDataToFirestore(userId: String) {
                val clientInfo = hashMapOf(
                    "firstName" to firstNameEditText.text.toString(),
                    "lastName" to lastNameEditText.text.toString(),
                    "middleName" to middleNameEditText.text.toString(),
                    "gender" to genderSpinner.selectedItem.toString(),
                    "birthdate" to birthdateEditText.text.toString(),
                    "contactNumber" to contactNumberEditText.text.toString(),
                    "idUrl" to idUrl,
                    "userType" to "CLIENT"
                )

                db.collection("users").document(userId)
                    .set(clientInfo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Client info saved successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("USER_TYPE", "CLIENT")
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving client info: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            private fun uploadSelfie(imageBitmap: Bitmap) {
                val file = createImageFileFromBitmap(imageBitmap)
                file?.let {
                    uploadImageToServer(file, auth.currentUser?.uid ?: "", "selfie") { imageUrl ->
                        // Handle success if needed
                    }
                }
            }

            private fun getFileFromUri(uri: Uri): File? {
                val inputStream = contentResolver.openInputStream(uri) ?: return null
                val file = File(cacheDir, getFileName(uri))
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                return file
            }

            private fun createImageFileFromBitmap(bitmap: Bitmap): File? {
                val file = File(cacheDir, "selfie.jpg")
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                return file
            }


            private fun uploadImageToServer(file: File, userId: String, imageType: String, onSuccess: (String) -> Unit) {
                val client = OkHttpClient()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("userId", userId)
                    .addFormDataPart("imageType", imageType)
                    .addFormDataPart("image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder()
                    .url("https://crimsonflare.space/image_upload.php")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@ClientAccountSetupActivity, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            response.body?.string()?.let { imageUrl ->
                                runOnUiThread {
                                    onSuccess(imageUrl)
                                    Toast.makeText(this@ClientAccountSetupActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@ClientAccountSetupActivity, "Failed to upload image: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }



            private fun toggleSaveButtonVisibility() {
                saveButton.visibility = if (isValidInput()) View.VISIBLE else View.GONE
            }

            private fun isValidInput(): Boolean {
                return !firstNameEditText.text.isNullOrEmpty() &&
                        !lastNameEditText.text.isNullOrEmpty() &&
                        !birthdateEditText.text.isNullOrEmpty() &&
                        validatePhoneNumber(contactNumberEditText.text.toString()) &&
                        selectedIdUri != null &&
                        selfieImageView.drawable != null // Ensures selfie is taken
            }

        }
