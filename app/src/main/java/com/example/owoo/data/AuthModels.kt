package com.example.owoo.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("phpsessid")
    val phpsessid: String?,
    val error: String?
)


data class ValidationRequest(
    val cookie: String
)

data class ValidationResponse(
    val valid: Boolean,
    val name: String?,
    val message: String?
)
