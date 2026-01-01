package com.example.neuroscan

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ScanResult(
    val imageBase64: String = "",
    val resultText: String = "",
    val timestamp: Long = 0,
    @get:Exclude var key: String? = null
)
