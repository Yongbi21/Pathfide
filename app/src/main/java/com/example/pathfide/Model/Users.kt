package com.example.pathfide.Model

data class User(
    var id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val email: String = "",
    val gender: String = "",
    val birthdate: String = "",
    val contactNumber: String = "",
    val profileImageUrl: String = "",
    val isOnline: Boolean = false,
    val userType: String = "",
    val accountStatus: String = "" // Add this line

)