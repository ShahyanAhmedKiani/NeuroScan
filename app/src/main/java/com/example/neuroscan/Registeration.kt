package com.example.neuroscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityRegisterationBinding
import com.example.neuroscan.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Registeration : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "RegisterationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnContinue.setOnClickListener { registerUser() }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {

        val name = binding.tilName.editText?.text.toString().trim()
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString().trim()
        val confirm = binding.tilRepeatPassword.editText?.text.toString().trim()
        val language = binding.etLanguage.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("All fields required")
            return
        }

        if (password != confirm) {
            toast("Passwords do not match")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val user = auth.currentUser!!

                // ✅ Send verification email and wait for success
                user.sendEmailVerification()
                    .addOnSuccessListener {

                        // ✅ Add user to Firestore AFTER email is sent
                        val userData = User(
                            uid = user.uid,
                            name = name,
                            email = email,
                            language = language,
                            emailVerified = false
                        )

                        db.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                toast("Account created. Verify your email first.")
                                auth.signOut() // Important: force user to verify before login
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                toast(it.message ?: "Database error")
                            }

                    }
                    .addOnFailureListener {
                        toast(it.message ?: "Failed to send verification email")
                    }

            }
            .addOnFailureListener {
                toast(it.message ?: "Registration failed")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}
