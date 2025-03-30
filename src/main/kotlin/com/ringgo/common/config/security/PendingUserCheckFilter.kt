package com.ringgo.common.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.ringgo.common.config.security.jwt.JwtUserDetails
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.exception.ErrorResponse
import com.ringgo.domain.user.entity.enums.UserStatus
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets

/**
 * PENDING 상태 사용자의 API 접근을 제한하는 필터
 */
@Component
class PendingUserCheckFilter(private val objectMapper: ObjectMapper) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 필터 예외 경로 확인
        if (isExcludedPath(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        // 현재 인증된 사용자 정보 가져오기
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication != null && authentication.principal is JwtUserDetails) {
            val userDetails = authentication.principal as JwtUserDetails
            val user = userDetails.user

            // PENDING 상태인 사용자 체크
            if (user.status == UserStatus.PENDING) {
                // 프로필 등록이 필요하다는 오류 응답 반환
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.characterEncoding = StandardCharsets.UTF_8.name()

                val errorResponse = ErrorResponse(ErrorCode.PROFILE_REQUIRED)
                response.writer.write(objectMapper.writeValueAsString(errorResponse))
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * 필터를 적용하지 않을 경로 확인
     */
    private fun isExcludedPath(path: String): Boolean {
        return path.startsWith("/auth/login") ||
                path.startsWith("/auth/authorize") ||
                path.startsWith("/auth/oauth/callback") ||
                path.startsWith("/auth/refresh") ||
                path.startsWith("/auth/signup/complete") ||
                path.startsWith("/auth/me/status") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/h2-console") ||
                path == "/favicon.ico" ||
                path.startsWith("/static")
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return isExcludedPath(request.requestURI)
    }
}
