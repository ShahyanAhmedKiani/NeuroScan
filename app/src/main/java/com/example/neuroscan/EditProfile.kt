package com.example.neuroscan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Load current user data
        loadUserProfile()

        // Set click listener for saving changes
        binding.btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        // TODO: Add click listener for btn_edit_picture to handle image selection
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.etName.setText(snapshot.child("name").getValue(String::class.java))
                    binding.etEmail.setText(snapshot.child("email").getValue(String::class.java))
                    binding.etDob.setText(snapshot.child("dateOfBirth").getValue(String::class.java))
                    binding.etCountry.setText(snapshot.child("country").getValue(String::class.java))

                    // TODO: Load profile image using a library like Glide or Picasso
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditProfile, "Failed to load profile.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveProfileChanges() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)

            val name = binding.etName.text.toString().trim()
            val dob = binding.etDob.text.toString().trim()
            val country = binding.etCountry.text.toString().trim()

            if (name.isEmpty() || dob.isEmpty() || country.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                return
            }

            val updatedData = mapOf(
                "name" to name,
                "dateOfBirth" to dob,
                "country" to country
            )

            userRef.updateChildren(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
