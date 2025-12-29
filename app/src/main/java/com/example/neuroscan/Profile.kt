package com.example.neuroscan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.neuroscan.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up button listeners
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        binding.btnLogout.setOnClickListener {
            startActivity(Intent(this, Logout::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Load or refresh user data every time the screen is shown
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.tvProfileName.text = snapshot.child("name").getValue(String::class.java)
                    binding.tvProfileEmail.text = snapshot.child("email").getValue(String::class.java)
                    binding.tvDob.text = snapshot.child("dateOfBirth").getValue(String::class.java)
                    binding.tvCountry.text = snapshot.child("country").getValue(String::class.java)

                    val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@Profile)
                            .load(imageUrl)
                            .placeholder(R.drawable.outline_account_circle_24)
                            .into(binding.ivProfilePicture)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Profile, "Failed to load profile.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
