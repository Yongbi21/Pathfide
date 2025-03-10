package com.example.pathfide.Model

data class Therapist(
    val fullName: String,
    val description: String,
    val location: String,
    val avatarUrl: String,
    val education: String = "",
    val affiliation: String = "",
    val clinicalHours: String = "",
    val onlineClinic: String = "",
    val physicianRate: String = " ",
    val userId: String =  ""
)
