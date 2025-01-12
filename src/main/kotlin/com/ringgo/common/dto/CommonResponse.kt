package com.ringgo.common.dto

data class CommonResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "Success"): CommonResponse<T> {
            return CommonResponse(200, message, data)
        }

        fun <T> created(data: T, message: String = "Created"): CommonResponse<T> {
            return CommonResponse(201, message, data)
        }

        fun error(status: Int, message: String): CommonResponse<Nothing> {
            return CommonResponse(status, message, null)
        }
    }
}