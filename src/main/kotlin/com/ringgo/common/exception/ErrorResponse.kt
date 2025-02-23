package com.ringgo.common.exception

data class ErrorResponse(
    val code: String,
    val message: String
) {
    constructor(errorCode: ErrorCode) : this(
        code = errorCode.code,
        message = errorCode.message
    )

    constructor(errorCode: ErrorCode, details: String) : this(
        code = errorCode.code,
        message = "${errorCode.message}: $details"
    )
}