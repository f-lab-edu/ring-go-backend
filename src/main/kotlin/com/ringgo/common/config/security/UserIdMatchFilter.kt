package com.ringgo.common.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.exception.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * 요청 헤더의 X-USER-ID와 현재 인증된 사용자 ID가 일치하는지 검증하는 필터
 */
@Component
class UserIdMatchFilter(private val objectMapper: ObjectMapper) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userIdHeader = request.getHeader("X-USER-ID")

        // X-USER-ID 헤더가 없는 경우 검증 스킵
        if (userIdHeader == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val requestUserId = UUID.fromString(userIdHeader)
            val authentication = SecurityContextHolder.getContext().authentication

            // 인증 정보가 없는 경우 다음 필터로 진행 (인증 필터에서 처리)
            if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
                filterChain.doFilter(request, response)
                return
            }

            val authenticatedUserId = UUID.fromString(authentication.name)

            // 사용자 ID 검증
            if (requestUserId != authenticatedUserId) {
                log.warn { "사용자 ID 불일치 - 인증된 사용자: $authenticatedUserId, 요청된 사용자: $requestUserId, URI: ${request.requestURI}" }
                handleUnauthorized(response)
                return
            }

            filterChain.doFilter(request, response)
        } catch (e: IllegalArgumentException) {
            // UUID 형식이 아닌 경우
            log.warn { "잘못된 X-USER-ID 형식: $userIdHeader, URI: ${request.requestURI}" }
            handleUnauthorized(response)
        }
    }

    private fun handleUnauthorized(response: HttpServletResponse) {
        val errorResponse = ErrorResponse(
            code = ErrorCode.USER_ID_MISMATCH.code,
            message = ErrorCode.USER_ID_MISMATCH.message
        )

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = ErrorCode.USER_ID_MISMATCH.status.value()
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
