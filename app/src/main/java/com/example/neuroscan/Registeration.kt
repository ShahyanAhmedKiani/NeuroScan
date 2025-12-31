package com.example.neuroscan

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityRegisterationBinding
import com.example.neuroscan.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class Registeration : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val TAG = "RegisterationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupCountryDropDown()
        setupDatePicker()

        binding.btnContinue.setOnClickListener { registerUser() }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupCountryDropDown() {
        val countries = resources.getStringArray(R.array.countries_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        binding.actvCountry.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.etDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.etDob.setText(selectedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val country = binding.actvCountry.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etRepeatPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || dob.isEmpty() || country.isEmpty() || password.isEmpty()) {
            toast("All fields required")
            return
        }

        if (password != confirm) {
            toast("Passwords do not match")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val firebaseUser = auth.currentUser!!

                val userData = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email,
                    dateOfBirth = dob,
                    country = country,
                    emailVerified = false
                )

                // Save to Realtime Database
                database.getReference("users").child(firebaseUser.uid).setValue(userData)

                // Send verification email
                firebaseUser.sendEmailVerification().addOnCompleteListener {
                    toast("Account created. Please check your email to verify your account.")
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity() // Clear all previous activities
                }
            }
            .addOnFailureListener {
                toast(it.message ?: "Registration failed")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
