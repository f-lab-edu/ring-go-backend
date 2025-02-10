package com.ringgo.common.exception

open class ApplicationException(
    val errorCode: ErrorCode,
    message: String? = errorCode.message
) : RuntimeException(message)