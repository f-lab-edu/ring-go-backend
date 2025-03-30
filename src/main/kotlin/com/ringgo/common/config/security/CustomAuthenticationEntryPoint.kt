package com.ringgo.common.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.exception.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

/**
 * 인증되지 않은 사용자(토큰 없음 또는 잘못된 토큰)에 대한 401 응답 처리
 */
@Component
class CustomAuthenticationEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        log.debug { "인증 실패 - 경로: ${request.requestURI}, 오류: ${authException.message}" }

        // 오류 타입에 따라 ErrorCode 선택
        val errorCode = determineErrorCode(request, authException)

        // 응답 설정
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"

        val errorResponse = ErrorResponse(errorCode)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }

    /**
     * 요청 속성과 예외 타입을 기반으로 적절한 오류 코드 결정
     */
    private fun determineErrorCode(request: HttpServletRequest, authException: AuthenticationException): ErrorCode {
        // JWT 예외 정보가 요청 속성에 저장되어 있는지 확인
        val jwtException = request.getAttribute("exception") as? Exception

        return when {
            // JWT 토큰 만료
            jwtException is ExpiredJwtException -> ErrorCode.TOKEN_EXPIRED

            // JWT 서명 오류
            jwtException is SignatureException -> ErrorCode.TOKEN_INVALID_SIGNATURE

            // JWT 형식 오류
            jwtException is MalformedJwtException -> ErrorCode.TOKEN_MALFORMED

            // 지원하지 않는 JWT 토큰
            jwtException is UnsupportedJwtException -> ErrorCode.TOKEN_UNSUPPORTED

            // 토큰 누락 (Authorization 헤더 없음)
            authException is InsufficientAuthenticationException &&
                    request.getHeader("Authorization").isNullOrBlank() -> ErrorCode.TOKEN_MISSING

            // 기타 인증 오류
            else -> ErrorCode.AUTHENTICATION_FAILED
        }
    }
}
