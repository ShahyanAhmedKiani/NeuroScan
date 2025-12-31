package com.example.neuroscan

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.neuroscan.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var selectedImageUri: Uri? = null
    private var existingImageBase64: String? = null

    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Initialization ---
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // --- Setup UI Components ---
        setupCountryDropDown()
        setupDatePicker()
        loadUserProfile()

        // --- Event Listeners ---
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnSaveChanges.setOnClickListener { saveProfileChanges() }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                Glide.with(this).load(uri).into(binding.ivProfilePicture)
            }
        }
        binding.btnEditPicture.setOnClickListener { galleryLauncher.launch("image/*") }
    }

    private fun setupCountryDropDown() {
        val countries = resources.getStringArray(R.array.countries_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        binding.actvCountry.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.etDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            // Try to parse the existing date to pre-set the calendar
            val currentDob = binding.etDob.text.toString()
            if (currentDob.isNotEmpty()) {
                try {
                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentDob)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (e: Exception) {
                    // Ignore if parsing fails
                }
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.etDob.setText(selectedDate)
            }, year, month, day).show()
        }
    }

    private fun loadUserProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        binding.etName.setText(sharedPreferences.getString("name", ""))
        binding.etEmail.setText(sharedPreferences.getString("email", ""))
        binding.etDob.setText(sharedPreferences.getString("dateOfBirth", ""))
        // Set the text for AutoCompleteTextView without filtering
        binding.actvCountry.setText(sharedPreferences.getString("country", ""), false)

        existingImageBase64 = sharedPreferences.getString("profileImageUrl", null)
        if (!existingImageBase64.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(existingImageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivProfilePicture.setImageBitmap(bitmap)
            } catch (e: IllegalArgumentException) {
                // Handle case where the string is not valid Base64
                binding.ivProfilePicture.setImageResource(R.drawable.outline_account_circle_24)
            }
        }
    }

    private fun saveProfileChanges() {
        val name = binding.etName.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val country = binding.actvCountry.text.toString().trim()

        if (name.isEmpty() || dob.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            val base64Image = uriToBase64(selectedImageUri!!)
            if (base64Image != null) {
                updateUserProfile(base64Image, name, dob, country)
            } else {
                Toast.makeText(this, "Failed to encode image.", Toast.LENGTH_SHORT).show()
            }
        } else {
            updateUserProfile(existingImageBase64, name, dob, country)
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun updateUserProfile(base64Image: String?, name: String, dob: String, country: String) {
        val userId = auth.currentUser?.uid ?: return

        val userRef = database.getReference("users").child(userId)
        val updatedData = mapOf(
            "name" to name,
            "dateOfBirth" to dob,
            "country" to country,
            "profileImageUrl" to (base64Image ?: "")
        )

        userRef.updateChildren(updatedData).addOnSuccessListener {
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("name", name)
                putString("dateOfBirth", dob)
                putString("country", country)
                if (base64Image != null) {
                    putString("profileImageUrl", base64Image)
                }
                apply()
            }
            Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
        }
    }
}
