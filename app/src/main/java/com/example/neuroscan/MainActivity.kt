package com.example.neuroscan

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Load user data
        loadAndDisplayUserData()

        // Click listeners for cards
        binding.btnStartScan.setOnClickListener { startActivity(Intent(this, ScanTumor::class.java)) }
        binding.cardAbout.setOnClickListener { startActivity(Intent(this, About::class.java)) }
        binding.cardResult.setOnClickListener { startActivity(Intent(this, Result::class.java)) }
        binding.cardBrainGames.setOnClickListener { startActivity(Intent(this, BrainGame::class.java)) }

        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_scan -> {
                    startActivity(Intent(this, ScanTumor::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadAndDisplayUserData() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "User")
        val profileImageBase64 = sharedPreferences.getString("profileImageUrl", null)

        binding.tvGreetingName.text = name

        if (!profileImageBase64.isNullOrEmpty()) {
            val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.ivProfile.setImageBitmap(bitmap)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        // Refresh user data in case it was updated in another activity
        loadAndDisplayUserData()
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
