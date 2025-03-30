package com.ringgo.domain.auth.dto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)