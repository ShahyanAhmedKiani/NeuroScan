package com.example.neuroscan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private val TAG = "SplashScreen"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Log.d(TAG, "onCreate")

        auth = FirebaseAuth.getInstance()

        // Delay for splash screen effect
        Handler(Looper.getMainLooper()).postDelayed({
            checkAutoLogin()
        }, 2000)
    }

    private fun checkAutoLogin() {
        val user = auth.currentUser

        if (user != null) {
            // Reload user to get latest email verification status
            user.reload().addOnCompleteListener {
                if (user.isEmailVerified) {
                    Log.d(TAG, "User already logged in and verified")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Please verify your email first", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        } else {
            Log.d(TAG, "No logged-in user")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
}
