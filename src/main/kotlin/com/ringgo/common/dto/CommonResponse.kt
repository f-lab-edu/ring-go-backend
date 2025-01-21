package com.ringgo.common.dto

data class CommonResponse(
    val status: Int,
    val message: String
) {
    companion object {
        fun error(status: Int, message: String): CommonResponse {
            return CommonResponse(status, message)
        }
    }
}