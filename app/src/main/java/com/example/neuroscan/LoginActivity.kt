package com.example.neuroscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val tag = "LoginActivity"

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(tag, "onCreate")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvToggleMode = findViewById<TextView>(R.id.tvToggleMode)

        btnLogin.setOnClickListener { loginUser() }

        tvToggleMode.setOnClickListener {
            startActivity(Intent(this, Registeration::class.java))
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Email and password required")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { 
                val user = auth.currentUser!!
                user.reload().addOnCompleteListener {
                    if (auth.currentUser!!.isEmailVerified) {
                        val userRef = database.getReference("users").child(user.uid)
                        userRef.child("emailVerified").setValue(true)

                        userRef.get().addOnSuccessListener { dataSnapshot ->
                            val name = dataSnapshot.child("name").getValue(String::class.java)
                            val userEmail = dataSnapshot.child("email").getValue(String::class.java)
                            val dob = dataSnapshot.child("dateOfBirth").getValue(String::class.java)
                            val country = dataSnapshot.child("country").getValue(String::class.java)

                            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            with(sharedPreferences.edit()) {
                                putString("name", name)
                                putString("email", userEmail)
                                putString("dateOfBirth", dob)
                                putString("country", country)
                                apply()
                            }

                            toast("Login successful")
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }.addOnFailureListener {
                            toast("Login successful, but failed to retrieve user data.")
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                    } else {
                        toast("Please verify your email first.")
                        auth.signOut()
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
        Log.d(tag, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
    }
}
