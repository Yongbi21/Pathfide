package com.example.mindpath.Activities//package com.example.chatmessenger.activities
//
//import android.app.Dialog
//import android.content.Intent
//import android.graphics.Color
//import android.graphics.drawable.ColorDrawable
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.KeyEvent
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.chatmessenger.Utils
//import com.example.chatmessenger.databinding.ActivitySendCodeBinding
//import com.example.chatmessenger.databinding.DialogWrongCodeBinding
//import com.example.chatmessenger.helper.FontFamilyHelper
//import com.example.chatmessenger.helper.FontSizeHelper
//import com.example.chatmessenger.helper.SendEmailTask
//import com.example.chatmessenger.utility.Utilities
//import com.example.chatmessenger.utility.VibrationUtil
//import com.example.mindpath.MainActivity
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.ValueEventListener
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.installations.Utils
//import java.util.Locale
//
///**
// * Activity to send verification code for account verification.
// */
//
//class SendCodeActivity : AppCompatActivity() {
//
//
//    private var codeValue: String = ""
//    private var email: String = ""
//    private lateinit var auth: FirebaseAuth
//    val firestore = FirebaseFirestore.getInstance()
//
//
//    lateinit var binding: ActivitySendCodeBinding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySendCodeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//
//        binding.text1.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // Code to execute before text changes
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Code to execute as text is changing
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s.toString().isNotEmpty()) {
//                    binding.text2.requestFocus()
//                }
//            }
//        })
//
//        binding.text2.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // Code to execute before text changes
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Code to execute as text is changing
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s.toString().isNotEmpty()) {
//                    binding.text3.requestFocus()
//                }
//            }
//        })
//
//        binding.text3.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // Code to execute before text changes
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Code to execute as text is changing
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s.toString().isNotEmpty()) {
//                    binding.text4.requestFocus()
//                }
//            }
//        })
//
//        binding.text4.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // Code to execute before text changes
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Code to execute as text is changing
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s.toString().isNotEmpty()) {
//                    binding.text5.requestFocus()
//                }
//            }
//        })
//
//        })
//
//
//
//        binding.text6.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Code to execute as text is changing
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s != null && s.length > 1) {
//                    // Remove all characters after the first one
//                    s.delete(1, s.length)
//                }
//            }
//        })
//
//
//
//
//        binding.text1.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && binding.text1.text.toString()
//                    .isEmpty()
//            ) {
//                binding.text1.requestFocus()
//                binding.text1.setText("")
//                return@OnKeyListener true
//            }
//            false
//        })
//
//        binding.text2.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && binding.text2.text.toString()
//                    .isEmpty()
//            ) {
//                binding.text1.requestFocus()
//                binding.text1.setText("")
//                return@OnKeyListener true
//            }
//            false
//        })
//
//
//        binding.text3.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && binding.text3.text.toString()
//                    .isEmpty()
//            ) {
//                binding.text2.requestFocus()
//                binding.text2.setText("")
//                return@OnKeyListener true
//            }
//            false
//        })
//
//        binding.text4.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && binding.text4.text.toString()
//                    .isEmpty()
//            ) {
//                binding.text3.requestFocus()
//                binding.text3.setText("")
//                return@OnKeyListener true
//            }
//            false
//        })
//
//
//
//        if (auth.currentUser != null) {
//            val userID = auth.currentUser?.uid ?: ""
//
//            firestore.collection("Users").document(userID)
//                .addSnapshotListener { documentSnapshot, exception ->
//                    if (exception != null) {
//                        // Handle the exception
//                        return@addSnapshotListener
//                    }
//
//                    if (documentSnapshot != null && documentSnapshot.exists()) {
//                        this.codeValue = documentSnapshot.getString("code").toString()
//                        this.email = documentSnapshot.getString("useremail").toString()
//                        Log.d("Baral", codeValue)
//                    }
//                }
//        }
//
//
//
//        binding.submitButton.setOnClickListener {
//            val t1 = binding.text1.text.toString()
//            val t2 = binding.text2.text.toString()
//            val t3 = binding.text3.text.toString()
//            val t4 = binding.text4.text.toString()
//
//
//            if (t1.isEmpty() || t2.isEmpty() || t3.isEmpty() || t4.isEmpty()  {
//                Toast.makeText(this, "Please input all 6-digit", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val resultCode = t1 + t2 + t3 + t4
//            if (resultCode != codeValue) {
//                val dialog = Dialog(this)
//                val viewBinding: DialogWrongCodeBinding =
//                    DialogWrongCodeBinding.inflate(LayoutInflater.from(this))
//                viewBinding.cancelButton.setOnClickListener { view1 -> dialog.dismiss() }
//                dialog.setCancelable(false)
//                dialog.setContentView(viewBinding.getRoot())
//                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                dialog.show()
//                return@setOnClickListener
//            }
//
//
//            updateAccountVerified()
//        }
//
//        binding.sendAgainButton.setOnClickListener{
//            updateCode()
//        }
//
//        refreshTextSize()
//    }
//
//    private fun updateCode() {
//        var verificationCode = Utilities.generateRandomCode()
//        SendEmailTask(email, verificationCode).execute()
//        val hashMapUser = hashMapOf(
//            "code" to verificationCode,
//        )
//
//        firestore.collection("Users").document(Utils.getUidLoggedIn())
//            .update(hashMapUser as Map<String, Any>)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Toast.makeText(this, "Resend 6-digit code succeed!", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//
//    private fun updateAccountVerified() {
//        val hashMapUser = hashMapOf(
//            "verified" to "true",
//        )
//
//        firestore.collection("Users").document(Utils.getUidLoggedIn())
//            .update(hashMapUser as Map<String, Any>)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Toast.makeText(this, "Verified account succeed!", Toast.LENGTH_SHORT).show()
//                    startActivity(Intent(this, MainActivity::class.java))
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                    finish()
//                }
//            }
//
//    }
//
//
//}