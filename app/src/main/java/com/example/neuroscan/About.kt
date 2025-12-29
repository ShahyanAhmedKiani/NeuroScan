package com.example.neuroscan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.neuroscan.databinding.ActivityAboutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class About : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar with navigation
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            // This will take the user back to the MainActivity or previous screen
            onBackPressedDispatcher.onBackPressed()
        }

        // Set click listener for the Privacy Policy button
        binding.btnPrivacy.setOnClickListener {
            showPrivacyPolicyDialog()
        }

        // TODO: Add an OnClickListener for btnWebsite to open a URL
        // binding.btnWebsite.setOnClickListener { ... }
    }

    private fun showPrivacyPolicyDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Privacy Policy")
            .setMessage(getPrivacyPolicyText())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getPrivacyPolicyText(): String {
        // This is placeholder text. You should replace this with your actual privacy policy.
        return "Last updated: December 25, 2025\n\n" +
                "NeuroScan is committed to protecting your privacy. This Privacy Policy explains how your personal information is collected, used, and disclosed by NeuroScan.\n\n" +
                "Information Collection and Use:\n" +
                "We collect information that you provide to us directly, such as when you create an account, upload an MRI scan, or communicate with us. This may include your name, email address, and health-related information contained in your scans.\n\n" +
                "Data Security:\n" +
                "The security of your data is important to us. We use industry-standard security measures to protect your information from unauthorized access, disclosure, alteration, and destruction.\n\n" +
                "Use of Scans:\n" +
                "MRI scans you upload are processed by our AI model for the sole purpose of providing you with an analysis. Scans are not shared with third parties without your explicit consent."
    }
}
