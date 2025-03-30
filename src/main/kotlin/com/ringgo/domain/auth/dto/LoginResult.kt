package com.ringgo.domain.auth.dto

import com.ringgo.common.config.security.jwt.TokenPair
import com.ringgo.domain.user.entity.User

data class LoginResult(
    val user: User,
    val tokenPair: TokenPair
)