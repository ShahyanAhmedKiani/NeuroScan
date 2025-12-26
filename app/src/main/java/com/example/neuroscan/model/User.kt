package com.example.neuroscan.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val emailVerified: Boolean = false
)
