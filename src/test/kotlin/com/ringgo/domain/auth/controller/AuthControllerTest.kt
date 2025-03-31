package com.ringgo.domain.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ringgo.common.config.security.jwt.TokenPair
import com.ringgo.common.config.security.oauth.api.OAuthProvider
import com.ringgo.common.config.security.oauth.core.OAuthProviderFactory
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.fixture.TestUser
import com.ringgo.domain.auth.dto.*
import com.ringgo.domain.auth.service.AuthService
import com.ringgo.domain.user.entity.enums.Provider
import com.ringgo.domain.user.service.UserService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authService: AuthService

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var oAuthProviderFactory: OAuthProviderFactory

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Value("\${oauth2.callback-base-url}")
    private lateinit var callbackBaseUrl: String

    // TestUser fixture 사용
    private val testUser = TestUser.createWithProfile()

    @BeforeEach
    fun setUpMockAuth() {
        val authentication = UsernamePasswordAuthenticationToken(
            testUser,
            null,
            emptyList()
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("인증 URL 생성 API")
    inner class GetAuthorizationUrl {

        @Test
        fun `유효한 소셜 로그인 제공자 요청 시 200을 응답한다`() {
            // given
            val provider = "google"
            val redirectUri = "$callbackBaseUrl/auth/oauth/callback/$provider"
            val authUrl = "https://accounts.google.com/o/oauth2/auth?client_id=123&redirect_uri=$redirectUri"
            val expectedResponse = mapOf("authUrl" to authUrl)

            val mockOauthProvider = mockk<OAuthProvider>()
            every { oAuthProviderFactory.getProvider(provider) } returns mockOauthProvider
            every { mockOauthProvider.getAuthorizationUrl(redirectUri) } returns authUrl
            every { mockOauthProvider.getProviderType() } returns Provider.GOOGLE

            // when & then
            mockMvc.perform(
                get("/auth/authorize/{provider}", provider)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())
        }

        @Test
        fun `유효하지 않은 제공자 요청 시 400을 응답한다`() {
            // given
            val provider = "invalid"

            every { oAuthProviderFactory.getProvider(provider) } throws ApplicationException(ErrorCode.INVALID_PROVIDER)

            // when & then
            mockMvc.perform(
                get("/auth/authorize/{provider}", provider)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("로그인 API")
    inner class Login {
        private val provider = "google"
        private val code = "test_auth_code"
        private val request = LoginRequest(code = code)

        @Test
        fun `로그인 성공 시 200을 응답한다`() {
            // given
            val tokenPair = TokenPair(
                accessToken = "access_token_123",
                refreshToken = "refresh_token_456"
            )

            val loginResult = LoginResult(
                user = testUser,
                tokenPair = tokenPair
            )

            val expectedResponse = LoginResponse(
                accessToken = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken,
                user = UserResponse(
                    id = testUser.id,
                    email = testUser.email,
                    name = testUser.name,
                    nickname = testUser.profile?.nickname,
                    profileImageUrl = testUser.profile?.imagePath,
                    role = testUser.role
                )
            )

            every { authService.login(provider, code) } returns loginResult

            // when & then
            mockMvc.perform(
                post("/auth/login/{provider}", provider)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())
        }

        @Test
        fun `유효하지 않은 제공자로 로그인 시 400을 응답한다`() {
            // given
            val invalidProvider = "invalid"

            every { authService.login(invalidProvider, code) } throws ApplicationException(ErrorCode.INVALID_PROVIDER)

            // when & then
            mockMvc.perform(
                post("/auth/login/{provider}", invalidProvider)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("OAuth 콜백 API")
    inner class OauthCallback {
        private val provider = "google"
        private val code = "test_auth_code"
        private val state = "test_state"

        @Test
        fun `OAuth 콜백 성공 시 200을 응답한다`() {
            // given
            val tokenPair = TokenPair(
                accessToken = "access_token_123",
                refreshToken = "refresh_token_456"
            )

            val loginResult = LoginResult(
                user = testUser,
                tokenPair = tokenPair
            )

            val expectedResponse = LoginResponse(
                accessToken = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken,
                user = UserResponse(
                    id = testUser.id,
                    email = testUser.email,
                    name = testUser.name,
                    nickname = testUser.profile?.nickname,
                    profileImageUrl = testUser.profile?.imagePath,
                    role = testUser.role
                )
            )

            val mockOauthProvider = mockk<OAuthProvider>()
            every { oAuthProviderFactory.getProvider(provider) } returns mockOauthProvider
            every { mockOauthProvider.getProviderType() } returns Provider.GOOGLE
            every {
                mockOauthProvider.validateCallback(
                    mapOf("code" to code, "state" to state),
                    any()
                )
            } returns true
            every { authService.login(provider, code) } returns loginResult

            // when & then
            mockMvc.perform(
                get("/auth/oauth/callback/{provider}", provider)
                    .param("code", code)
                    .param("state", state)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())
        }

        @Test
        fun `콜백 검증 실패 시 401을 응답한다`() {
            // given
            val mockOauthProvider = mockk<OAuthProvider>()
            every { oAuthProviderFactory.getProvider(provider) } returns mockOauthProvider
            every { mockOauthProvider.getProviderType() } returns Provider.GOOGLE
            every {
                mockOauthProvider.validateCallback(
                    mapOf("code" to code, "state" to state),
                    any()
                )
            } returns false

            // when & then
            mockMvc.perform(
                get("/auth/oauth/callback/{provider}", provider)
                    .param("code", code)
                    .param("state", state)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isUnauthorized)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("토큰 갱신 API")
    inner class RefreshToken {
        private val refreshToken = "refresh_token_123"

        @Test
        fun `토큰 갱신 성공 시 200을 응답한다`() {
            // given
            val tokenPair = TokenPair(
                accessToken = "new_access_token_123",
                refreshToken = "new_refresh_token_456"
            )

            val expectedResponse = TokenResponse(
                accessToken = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken
            )

            every { authService.refresh(refreshToken) } returns tokenPair

            // when & then
            mockMvc.perform(
                post("/auth/refresh")
                    .header("Refresh-Token", refreshToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())
        }

        @Test
        fun `유효하지 않은 리프레시 토큰으로 요청 시 401을 응답한다`() {
            // given
            val invalidToken = "invalid_token"

            every { authService.refresh(invalidToken) } throws ApplicationException(ErrorCode.INVALID_REFRESH_TOKEN)

            // when & then
            mockMvc.perform(
                post("/auth/refresh")
                    .header("Refresh-Token", invalidToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isUnauthorized)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    inner class Logout {

        @Test
        fun `로그아웃 성공 시 200을 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

            every { authService.logout(userId) } just runs

            // when & then
            mockMvc.perform(
                post("/auth/logout")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andDo(print())
        }

        @Test
        fun `존재하지 않는 사용자 로그아웃 시 404를 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

            every { authService.logout(userId) } throws ApplicationException(ErrorCode.USER_NOT_FOUND)

            // when & then
            mockMvc.perform(
                post("/auth/logout")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 API")
    inner class Withdraw {

        @Test
        fun `회원 탈퇴 성공 시 200을 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

            every { authService.withdraw(userId) } just runs

            // when & then
            mockMvc.perform(
                delete("/auth/withdraw")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andDo(print())
        }

        @Test
        fun `존재하지 않는 사용자 탈퇴 시 404를 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

            every { authService.withdraw(userId) } throws ApplicationException(ErrorCode.USER_NOT_FOUND)

            // when & then
            mockMvc.perform(
                delete("/auth/withdraw")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("회원가입 완료 API")
    inner class CompleteSignup {
        private val request = CompleteSignupRequest(
            nickname = "희진",
            profileImage = "profile.jpg"
        )

        @Test
        fun `회원가입 완료 성공 시 200을 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

            val expectedResponse = UserResponse(
                id = testUser.id,
                email = testUser.email,
                name = testUser.name,
                nickname = testUser.profile?.nickname,
                profileImageUrl = testUser.profile?.imagePath,
                role = testUser.role
            )

            every {
                userService.completeSignup(userId, request.nickname, request.profileImage)
            } returns testUser

            // when & then
            mockMvc.perform(
                post("/auth/signup/complete")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())
        }

        @Test
        fun `존재하지 않는 사용자의 회원가입 완료 시 404를 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

            every {
                userService.completeSignup(userId, request.nickname, request.profileImage)
            } throws ApplicationException(ErrorCode.USER_NOT_FOUND)

            // when & then
            mockMvc.perform(
                post("/auth/signup/complete")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andDo(print())
        }

        @Test
        fun `비활성 사용자의 회원가입 완료 시 403을 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

            every {
                userService.completeSignup(userId, request.nickname, request.profileImage)
            } throws ApplicationException(ErrorCode.INACTIVE_USER)

            // when & then
            mockMvc.perform(
                post("/auth/signup/complete")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
                .andDo(print())
        }

        @Test
        fun `닉네임이 없는 요청 시 400을 응답한다`() {
            // given
            val userId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")
            val invalidRequest = CompleteSignupRequest(
                nickname = "",
                profileImage = "profile.jpg"
            )

            // 빈 닉네임에 대한 서비스 호출 모킹 추가
            every {
                userService.completeSignup(userId, "", invalidRequest.profileImage)
            } throws ApplicationException(ErrorCode.INVALID_INPUT_VALUE)

            // when & then
            mockMvc.perform(
                post("/auth/signup/complete")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }
}
