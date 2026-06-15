package ru.taf.lab_4.model

data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_expires_in: Int
)