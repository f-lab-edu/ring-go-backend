package com.ringgo.common.exception

import com.ringgo.common.dto.CommonResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException) = ResponseEntity
        .status(e.errorCode.status)
        .body(CommonResponse.error(e.errorCode.status.value(), e.errorCode.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error { "handleMethodArgumentNotValidException: $e" }
        val errorResponse = ErrorResponse(
            code = ErrorCode.INVALID_INPUT_VALUE.code,
            message = e.bindingResult.fieldError?.defaultMessage ?: "잘못된 입력값입니다"
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error { "handleException: $e" }
        val errorResponse = ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(errorResponse)
    }
}