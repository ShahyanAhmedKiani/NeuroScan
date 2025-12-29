package com.example.neuroscan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityLogoutBinding
import com.google.firebase.auth.FirebaseAuth

class Logout : AppCompatActivity() {

    private lateinit var binding: ActivityLogoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Listener for the "Stay Signed In" button
        binding.btnCancel.setOnClickListener {
            // Finishes this activity and returns to the previous one (MainActivity)
            finish()
        }

        // Listener for the "Sign Out" button
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            // Clear all previous activities from the back stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
