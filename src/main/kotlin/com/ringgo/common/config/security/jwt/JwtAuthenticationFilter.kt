package com.ringgo.common.config.security.jwt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

/**
 * JWT 토큰 기반 인증 필터
 */
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = resolveToken(request)

            if (token != null) {
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        val authentication = jwtTokenProvider.getAuthentication(token)
                        SecurityContextHolder.getContext().authentication = authentication
                        log.debug { "인증 성공: ${authentication.name}" }
                    } else {
                        log.debug { "유효하지 않은 토큰: ${maskToken(token)}" }
                    }
                } catch (e: ExpiredJwtException) {
                    log.debug { "만료된 JWT 토큰: ${e.message}" }
                    request.setAttribute("exception", e)
                } catch (e: SignatureException) {
                    log.debug { "잘못된 JWT 서명: ${e.message}" }
                    request.setAttribute("exception", e)
                } catch (e: MalformedJwtException) {
                    log.debug { "잘못된 형식의 JWT 토큰: ${e.message}" }
                    request.setAttribute("exception", e)
                } catch (e: UnsupportedJwtException) {
                    log.debug { "지원되지 않는 JWT 토큰: ${e.message}" }
                    request.setAttribute("exception", e)
                } catch (e: JwtException) {
                    log.debug { "JWT 토큰 처리 중 오류: ${e.message}" }
                    request.setAttribute("exception", e)
                }
            }
        } catch (e: Exception) {
            log.error(e) { "JWT 인증 처리 중 오류 발생" }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    private fun maskToken(token: String): String {
        return if (token.length > 10) {
            "${token.substring(0, 5)}...${token.substring(token.length - 5)}"
        } else {
            "***"
        }
    }
}
