package com.example.pathfide.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.pathfide.MainActivity
import com.example.pathfide.R
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SigninActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var loginButton: Button
    private lateinit var signupButton: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var forgotPasswordText: TextView
    private lateinit var settingsButton: Button
    private lateinit var loginFormScroll: View
    private lateinit var googleLoginButton: ImageView
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private var userType: String? = null

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        emailEditText = findViewById(R.id.loginetemail)
        passwordEditText = findViewById(R.id.loginetpassword)
        forgotPasswordText = findViewById(R.id.forgotPassword)
        settingsButton = findViewById(R.id.settingsButton)
        loginFormScroll = findViewById(R.id.login_form_scroll)
        googleLoginButton = findViewById(R.id.googleButton)

        callbackManager = CallbackManager.Factory.create()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }

        loginButton.setOnClickListener {
            signInWithEmailPassword()
        }

        signupButton.setOnClickListener {
            navigateToSignup()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, LanguageSettingsActivity::class.java)
            startActivity(intent)
        }
        checkUserSession()

        forgotPasswordText.setOnClickListener {
            loginFormScroll.visibility = View.GONE

            // Get NavHostFragment and ensure it is not null
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_password)
            if (navHostFragment is NavHostFragment) {
                val navController = navHostFragment.findNavController()
                navController.navigate(R.id.emailInputFragment)

                // Make the fragment container visible
                findViewById<View>(R.id.nav_host_fragment_password).visibility = View.VISIBLE
            } else {
                Log.e("SigninActivity", "NavHostFragment is null or not found")
            }
        }

        cleanupPreviousSession()

    }

    private fun checkUserSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("SigninActivity", "User session found: ${currentUser.email}")

            // Retrieve user type from Firestore before navigating to MainActivity
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userType = document.getString("userType") ?: "CLIENT"
                        navigateToMain(userType)
                    } else {
                        Log.w("SigninActivity", "User document not found, navigating to setup.")
                        navigateToSetup()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SigninActivity", "Failed to fetch user data: ${e.message}")
                }
        } else {
            Log.d("SigninActivity", "No active user session found.")
        }
    }


    private fun cleanupPreviousSession() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .update("isOnline", false, "lastSeen", System.currentTimeMillis())
                .addOnFailureListener { e ->
                    Log.e("SigninActivity", "Error updating session: ${e.message}")
                }
        } else {
            Log.d("SigninActivity", "No user session to clean up.")
        }
    }


    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInWithEmailPassword() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid email"
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        // Firebase Login
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val userType = document.getString("userType")
                                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                                    // Redirect to MainActivity with userType
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("USER_TYPE", userType)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error retrieving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToSignup() {
        // Set userType to "CLIENT" by default
        val userType = "CLIENT"

        val intent = Intent(this, SignupActivity::class.java)
        intent.putExtra("USER_TYPE", userType)

        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle Facebook login result
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "Google sign-in successful for user: ${auth.currentUser?.email}")
                } else {
                    Log.e("GoogleSignIn", "Google sign-in failed: ${task.exception?.message}")
                }
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Google login successful!", Toast.LENGTH_SHORT).show()

                    val userId = user?.uid
                    if (userId != null) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // Existing user - retrieve userType and go to MainActivity
                                    val userTypeFromDocument = document.getString("userType") ?: "CLIENT"
                                    navigateToMain(userTypeFromDocument)
                                } else {
                                    // New user - direct to ClientAccountSetupActivity
                                    val newUser = hashMapOf(
                                        "email" to user.email,
                                        "userType" to "CLIENT",  // Default userType
                                        "isOnline" to true,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    db.collection("users").document(userId)
                                        .set(newUser)
                                        .addOnSuccessListener {
                                            Log.d("SigninActivity", "New user added to Firestore")
                                            navigateToSetup()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("SigninActivity", "Error adding user to Firestore: ${e.message}")
                                            Toast.makeText(this, "Failed to create user profile.", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("SigninActivity", "Firestore error: ${e.message}")
                            }
                    } else {
                        Log.e("SigninActivity", "User ID is null")
                    }
                } else {
                    Log.e("SigninActivity", "Google login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Google login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMain(userType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Ensure a fresh instance
        intent.putExtra("USER_TYPE", userType)
        startActivity(intent)
        finish()
    }

    private fun navigateToSetup() {
        val intent = Intent(this, ClientAccountSetupActivity::class.java)
        startActivity(intent)
        finish()
    }


}