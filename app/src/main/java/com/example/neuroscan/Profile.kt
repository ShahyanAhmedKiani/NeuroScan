package com.example.neuroscan

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnEditProfile.setOnClickListener { startActivity(Intent(this, EditProfile::class.java)) }
        binding.btnLogout.setOnClickListener { startActivity(Intent(this, Logout::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", null)
        val email = sharedPreferences.getString("email", null)
        val dob = sharedPreferences.getString("dateOfBirth", null)
        val country = sharedPreferences.getString("country", null)
        val profileImageBase64 = sharedPreferences.getString("profileImageUrl", null)

        if (name != null && email != null && dob != null && country != null) {
            binding.tvProfileName.text = name
            binding.tvProfileEmail.text = email
            binding.tvDob.text = dob
            binding.tvCountry.text = country

            if (!profileImageBase64.isNullOrEmpty()) {
                val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivProfilePicture.setImageBitmap(bitmap)
            }
        } else {
            fetchUserProfileFromFirebase()
        }
    }

    private fun fetchUserProfileFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        val userRef = database.getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val dob = snapshot.child("dateOfBirth").getValue(String::class.java)
                val country = snapshot.child("country").getValue(String::class.java)
                val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("name", name)
                    putString("email", email)
                    putString("dateOfBirth", dob)
                    putString("country", country)
                    putString("profileImageUrl", imageUrl)
                    apply()
                }

                binding.tvProfileName.text = name
                binding.tvProfileEmail.text = email
                binding.tvDob.text = dob
                binding.tvCountry.text = country

                if (!imageUrl.isNullOrEmpty()) {
                    val imageBytes = Base64.decode(imageUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.ivProfilePicture.setImageBitmap(bitmap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
