package com.ringgo.domain.auth.controller

import com.ringgo.common.config.security.oauth.core.OAuthProviderFactory
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.auth.dto.*
import com.ringgo.domain.auth.service.AuthService
import com.ringgo.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val oAuthProviderFactory: OAuthProviderFactory,
    @Value("\${oauth2.callback-base-url}") private val callbackBaseUrl: String
) {
    /**
     * 인증 URL 생성 엔드포인트
     * 클라이언트에서 사용자를 리다이렉트할 URL 반환
     */
    @GetMapping("/authorize/{provider}")
    fun getAuthorizationUrl(
        @PathVariable provider: String
    ): ResponseEntity<Map<String, String>> {
        log.debug { "인증 URL 요청됨 - 제공자: $provider" }

        try {
            val oauthProvider = oAuthProviderFactory.getProvider(provider)
            val redirectUri = "$callbackBaseUrl/auth/oauth/callback/${provider.lowercase()}"
            val authUrl = oauthProvider.getAuthorizationUrl(redirectUri)

            return ResponseEntity.ok(mapOf("authUrl" to authUrl))
        } catch (e: Exception) {
            log.error { "인증 URL 생성 실패 - 제공자: $provider" }
            throw ApplicationException(ErrorCode.INVALID_PROVIDER)
        }
    }

    /**
     * 로그인 엔드포인트 (모바일 또는 백엔드 API 호출용)
     */
    @PostMapping("/login/{provider}")
    fun login(
        @PathVariable provider: String,
        @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val loginResult = authService.login(provider, request.code)
        return ResponseEntity.ok(
            LoginResponse(
                accessToken = loginResult.tokenPair.accessToken,
                refreshToken = loginResult.tokenPair.refreshToken,
                user = UserResponse(
                    id = loginResult.user.id,
                    email = loginResult.user.email,
                    name = loginResult.user.name,
                    nickname = loginResult.user.profile?.nickname,
                    profileImageUrl = loginResult.user.profile?.imagePath,
                    role = loginResult.user.role
                )
            )
        )
    }

    /**
     * OAuth 콜백 엔드포인트 (웹 브라우저 리다이렉트용)
     */
    @GetMapping("/oauth/callback/{provider}")
    fun oauthCallback(
        @PathVariable provider: String,
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        request: HttpServletRequest
    ): ResponseEntity<LoginResponse> {
        log.debug { "OAuth 콜백 수신됨 - 제공자: $provider, 상태: $state" }

        // 공급자별 콜백 검증
        val oauthProvider = oAuthProviderFactory.getProvider(provider)
        val params = mapOf(
            "code" to code,
            "state" to (state ?: "")
        )

        if (!oauthProvider.validateCallback(params, request)) {
            log.warn { "잘못된 콜백 검증 - 제공자: $provider" }
            throw ApplicationException(ErrorCode.AUTHENTICATION_FAILED)
        }

        val loginResult = authService.login(provider, code)
        return ResponseEntity.ok(
            LoginResponse(
                accessToken = loginResult.tokenPair.accessToken,
                refreshToken = loginResult.tokenPair.refreshToken,
                user = UserResponse(
                    id = loginResult.user.id,
                    email = loginResult.user.email,
                    name = loginResult.user.name,
                    nickname = loginResult.user.profile?.nickname,
                    profileImageUrl = loginResult.user.profile?.imagePath,
                    role = loginResult.user.role
                )
            )
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader("Refresh-Token") refreshToken: String
    ): ResponseEntity<TokenResponse> {
        val tokenPair = authService.refresh(refreshToken)
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken
            )
        )
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("X-USER-ID") userId: UUID
    ): ResponseEntity<Unit> {
        authService.logout(userId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/withdraw")
    fun withdraw(
        @RequestHeader("X-USER-ID") userId: UUID
    ): ResponseEntity<Unit> {
        authService.withdraw(userId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/signup/complete")
    fun completeSignup(
        @RequestHeader("X-USER-ID") userId: UUID,
        @RequestBody request: CompleteSignupRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.completeSignup(
            userId = userId,
            nickname = request.nickname,
            profileImage = request.profileImage
        )

        return ResponseEntity.ok(
            UserResponse(
                id = user.id,
                email = user.email,
                name = user.name,
                nickname = user.profile?.nickname,
                profileImageUrl = user.profile?.imagePath,
                role = user.role
            )
        )
    }
}
