package com.ringgo.domain.auth.dto

data class CompleteSignupRequest(
    val nickname: String,
    val profileImage: String? = null
)