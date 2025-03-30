package com.ringgo.domain.auth.dto

import com.ringgo.domain.user.entity.enums.UserRole
import java.util.*

data class UserResponse(
    val id: UUID,
    val email: String,
    val name: String,
    val nickname: String?,
    val profileImageUrl: String?,
    val role: UserRole
)