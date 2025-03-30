package com.ringgo.common.config.security.jwt

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.time.Duration
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-validity}") private val accessTokenValidity: Long,
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long,
    private val redisTemplate: StringRedisTemplate,
    private val jwtUserDetailsService: JwtUserDetailsService
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    // Redis 키 접두사
    private val tokenBlacklistPrefix = "token:blacklist:"

    /**
     * 액세스 토큰 생성
     */
    fun createAccessToken(userId: String, role: String): String {
        val claims = Jwts.claims().setSubject(userId)
        claims["role"] = role
        val now = Date()
        val validity = Date(now.time + accessTokenValidity)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 리프레시 토큰 생성
     */
    fun createRefreshToken(userId: String): String {
        val now = Date()
        val validity = Date(now.time + refreshTokenValidity)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 토큰 유효성 검증
     * JwtException을 발생시켜 구체적인 오류 유형 전달
     */
    fun validateToken(token: String): Boolean {
        // 블랙리스트 확인 (로그아웃된 토큰인지)
        if (isTokenBlacklisted(token)) {
            log.debug { "블랙리스트에 등록된 토큰" }
            return false
        }

        // 호출자(JwtAuthenticationFilter)에게 예외 전파
        val claims = getClaims(token)

        // 만료 시간 검증
        return !claims.expiration.before(Date())
    }

    /**
     * 리프레시 토큰 유효성 검증
     */
    fun validateRefreshToken(token: String): Boolean = validateToken(token)

    /**
     * 토큰으로부터 인증 객체 생성
     */
    fun getAuthentication(token: String): Authentication {
        try {
            val claims = getClaims(token)

            // jwtUserDetailsService를 통해 사용자 정보 로드
            val userDetails = jwtUserDetailsService.loadUserByUsername(claims.subject)

            return UsernamePasswordAuthenticationToken(
                userDetails,   // principal을 UserDetails 구현체로 변경
                "",           // credentials (비밀번호는 비워둠)
                userDetails.authorities  // 권한 정보
            )
        } catch (e: Exception) {
            log.error(e) { "인증 객체 생성 중 오류" }
            throw ApplicationException(ErrorCode.INVALID_TOKEN)
        }
    }

    /**
     * 토큰으로부터 사용자 ID 추출
     */
    fun getUserId(token: String): UUID {
        try {
            return UUID.fromString(getClaims(token).subject)
        } catch (e: Exception) {
            log.error(e) { "토큰에서 사용자 ID 추출 중 오류" }
            throw ApplicationException(ErrorCode.INVALID_TOKEN)
        }
    }

    /**
     * 토큰으로부터 사용자 역할 추출
     */
    fun getUserRole(token: String): String {
        try {
            return getClaims(token)["role"].toString()
        } catch (e: Exception) {
            log.error(e) { "토큰에서 역할 추출 중 오류" }
            throw ApplicationException(ErrorCode.INVALID_TOKEN)
        }
    }

    /**
     * 로그아웃 처리 - 토큰 블랙리스트에 추가
     */
    fun blacklistToken(token: String) {
        try {
            val claims = getClaims(token)
            val expiration = claims.expiration
            val ttl = expiration.time - System.currentTimeMillis()

            if (ttl > 0) {
                val key = tokenBlacklistPrefix + token
                redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(ttl))
                log.debug { "토큰 블랙리스트 등록 완료" }
            }
        } catch (e: Exception) {
            log.error(e) { "토큰 블랙리스트 등록 중 오류" }
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    private fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey(tokenBlacklistPrefix + token)
    }

    /**
     * 토큰으로부터 클레임 추출
     * JWT 예외를 발생시켜 호출자에게 전파
     */
    private fun getClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
}
