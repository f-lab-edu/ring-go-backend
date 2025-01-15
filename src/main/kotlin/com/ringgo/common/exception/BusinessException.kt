package com.ringgo.common.exception

open class BusinessException(
    val errorCode: ErrorCode,
    message: String? = errorCode.message
) : RuntimeException(message)