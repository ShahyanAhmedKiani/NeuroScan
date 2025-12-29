package com.example.neuroscan.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val country: String = "",
    val profileImageUrl: String = "",
    val emailVerified: Boolean = false
)
