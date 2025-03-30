package com.ringgo.common.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.exception.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * 인증은 됐지만 권한이 없는 사용자에 대한 403 응답 처리
 */
@Component
class CustomAccessDeniedHandler(private val objectMapper: ObjectMapper) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        log.debug { "권한 부족 - 경로: ${request.requestURI}, 오류: ${accessDeniedException.message}" }

        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val errorResponse = ErrorResponse(
            code = ErrorCode.NOT_MEETING_MEMBER.code,
            message = ErrorCode.NOT_MEETING_MEMBER.message
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
