package com.ringgo.domain.auth.controller

import com.ringgo.common.config.security.oauth.core.OAuthProviderFactory
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.auth.dto.*
import com.ringgo.domain.auth.service.AuthService
import com.ringgo.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

private val log = KotlinLogging.logger {}

@Tag(name = "Auth", description = "인증 API")
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
    @Operation(summary = "인증 URL 생성", description = "소셜 로그인을 위한 인증 URL을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 URL 생성 성공"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 제공자")
        ]
    )
    @GetMapping("/authorize/{provider}")
    fun getAuthorizationUrl(
        @Parameter(description = "소셜 로그인 제공자 (google, kakao, naver, apple)")
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
    @Operation(summary = "소셜 로그인", description = "소셜 로그인 인증 코드로 로그인을 진행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @PostMapping("/login/{provider}")
    fun login(
        @Parameter(description = "소셜 로그인 제공자 (google, kakao, naver, apple)")
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
    @Operation(summary = "OAuth 콜백", description = "소셜 로그인 제공자로부터 리다이렉트되는 콜백을 처리합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @GetMapping("/oauth/callback/{provider}")
    fun oauthCallback(
        @Parameter(description = "소셜 로그인 제공자 (google, kakao, naver, apple)")
        @PathVariable provider: String,
        @Parameter(description = "인증 코드")
        @RequestParam code: String,
        @Parameter(description = "상태 값 (선택 사항)")
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

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
        ]
    )
    @PostMapping("/refresh")
    fun refresh(
        @Parameter(description = "리프레시 토큰")
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

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리를 합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @PostMapping("/logout")
    fun logout(
        @Parameter(description = "사용자 ID")
        @RequestHeader("X-USER-ID") userId: UUID
    ): ResponseEntity<Unit> {
        authService.logout(userId)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 비활성화합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @DeleteMapping("/withdraw")
    fun withdraw(
        @Parameter(description = "사용자 ID")
        @RequestHeader("X-USER-ID") userId: UUID
    ): ResponseEntity<Unit> {
        authService.withdraw(userId)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "회원가입 완료", description = "추가 정보를 입력하여 회원가입 절차를 완료합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "회원가입 완료 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @PostMapping("/signup/complete")
    fun completeSignup(
        @Parameter(description = "사용자 ID")
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
