package com.ringgo.common.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(e: ApplicationException): ResponseEntity<ErrorResponse> {
        log.warn { "handleApplicationException: $e" }
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse(e.errorCode))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        log.warn { "handleMethodArgumentNotValidException: $e" }
        val fieldError = e.bindingResult.fieldError
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponse(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "${fieldError?.field}: ${fieldError?.defaultMessage}"
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        e: MethodArgumentTypeMismatchException
    ): ResponseEntity<ErrorResponse> {
        log.warn { "handleMethodArgumentTypeMismatchException: $e" }
        return ResponseEntity
            .status(ErrorCode.INVALID_DATE_FORMAT.status)
            .body(ErrorResponse(ErrorCode.INVALID_DATE_FORMAT))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        e: MissingServletRequestParameterException
    ): ResponseEntity<ErrorResponse> {
        log.warn { "handleMissingServletRequestParameterException: $e" }
        return ResponseEntity
            .status(ErrorCode.MISSING_PARAMETER.status)
            .body(ErrorResponse(ErrorCode.MISSING_PARAMETER, e.parameterName))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error { "handleException: $e" }
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR))
    }
}