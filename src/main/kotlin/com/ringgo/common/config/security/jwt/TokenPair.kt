package com.ringgo.common.config.security.jwt

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)