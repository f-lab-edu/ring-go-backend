package com.ringgo.common.dto

sealed class CommonResponse<T> {
    data class Success<T>(
        val data: T
    ) : CommonResponse<T>()

    data class Error(
        val status: Int,
        val message: String
    ) : CommonResponse<Nothing>()

    companion object {
        fun <T> success(data: T): CommonResponse<T> = Success(data)

        fun error(status: Int, message: String): CommonResponse<Nothing> =
            Error(status, message)
    }
}