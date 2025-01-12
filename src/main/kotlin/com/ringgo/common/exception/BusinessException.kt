package com.ringgo.common.exception

open class BusinessException : RuntimeException {
    val errorCode: ErrorCode

    constructor(errorCode: ErrorCode): super(errorCode.message) {
        this.errorCode = errorCode
    }

    constructor(message: String, errorCode: ErrorCode): super(message) {
        this.errorCode = errorCode
    }
}