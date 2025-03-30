package com.ringgo.domain.auth.dto

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode

data class UserInfo(
    val email: String,
    val name: String,
    val providerId: String,
    val profileImageUrl: String?
) {
    init {
        if (email.isBlank()) {
            throw ApplicationException(ErrorCode.EMAIL_REQUIRED)
        }
        if (name.isBlank()) {
            throw ApplicationException(ErrorCode.NAME_REQUIRED)
        }
        if (providerId.isBlank()) {
            throw ApplicationException(ErrorCode.PROVIDER_ID_REQUIRED)
        }
    }
}