package com.example.neuroscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Fetch user name
        fetchUserName()

        // Click listeners for cards
        binding.btnStartScan.setOnClickListener {

            startActivity(Intent(this, ScanTumor::class.java))
        }

        binding.cardAbout.setOnClickListener {

            startActivity(Intent(this, About::class.java))
        }

        binding.cardResult.setOnClickListener {
            startActivity(Intent(this, Result::class.java))
        }

        binding.cardBrainGames.setOnClickListener {

           startActivity(Intent(this, BrainGame::class.java))
        }

        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on home screen, do nothing
                    true
                }
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

    private fun fetchUserName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    if (name != null) {
                        binding.tvGreetingName.text = name
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Log.e(TAG, "Failed to fetch user name", error.toException())
                }
            })
        }
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
