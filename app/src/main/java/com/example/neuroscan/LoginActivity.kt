package com.example.neuroscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener { loginUser() }

        binding.btnBack.setOnClickListener { finish() }

        binding.tvToggleMode.setOnClickListener {
            startActivity(Intent(this, Registeration::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Email and password required")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val user = auth.currentUser
                if (user == null) {
                    toast("Login failed")
                    return@addOnSuccessListener
                }

                // Reload user to get latest verification status
                user.reload().addOnCompleteListener {

                    if (!user.isEmailVerified) {
                        toast("Please verify your email first")
                        auth.signOut()
                        return@addOnCompleteListener
                    }

                    // Update Firestore emailVerified field
                    db.collection("users").document(user.uid)
                        .update("emailVerified", true)
                        .addOnSuccessListener {
                            // Optional: Log success
                        }
                        .addOnFailureListener {
                            toast("Failed to update verification status in DB")
                        }

                    // Verify user exists in Firestore
                    db.collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                toast("Login successful")
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                toast("User record not found")
                                auth.signOut()
                            }
                        }
                        .addOnFailureListener {
                            toast(it.message ?: "Database error")
                        }
                }
            }
            .addOnFailureListener {
                toast(it.message ?: "Invalid credentials")
            }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
