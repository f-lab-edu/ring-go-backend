package com.ringgo.domain.auth.service

import com.ringgo.common.config.security.jwt.JwtTokenProvider
import com.ringgo.common.config.security.jwt.TokenPair
import com.ringgo.common.config.security.oauth.api.OAuthProvider
import com.ringgo.common.config.security.oauth.core.OAuthProviderFactory
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.fixture.TestUser
import com.ringgo.domain.auth.dto.UserInfo
import com.ringgo.domain.user.entity.User
import com.ringgo.domain.user.entity.UserConnection
import com.ringgo.domain.user.entity.enums.Provider
import com.ringgo.domain.user.entity.enums.UserRole
import com.ringgo.domain.user.entity.enums.UserStatus
import com.ringgo.domain.user.repository.UserConnectionRepository
import com.ringgo.domain.user.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class AuthServiceTest {

    private lateinit var authService: AuthService
    private lateinit var userRepository: UserRepository
    private lateinit var userConnectionRepository: UserConnectionRepository
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var oAuthProviderFactory: OAuthProviderFactory
    private lateinit var oAuthProvider: OAuthProvider

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userConnectionRepository = mockk()
        jwtTokenProvider = mockk()
        oAuthProviderFactory = mockk()
        oAuthProvider = mockk()

        authService = AuthService(
            userRepository,
            userConnectionRepository,
            jwtTokenProvider,
            oAuthProviderFactory
        )
    }

    @Nested
    @DisplayName("소셜 로그인 테스트")
    inner class LoginTest {
        private val provider = "google"
        private val code = "auth_code_123"
        private val userId = TestUser.USER_ID
        private val email = "heejin@test.com"
        private val name = "전희진"
        private val providerId = "google_user_id_123"
        private val socialToken = "social_token_123"

        private val userInfo = UserInfo(
            providerId = providerId,
            email = email,
            name = name,
            profileImageUrl = null
        )

        @Test
        fun `소셜 로그인 성공 - 신규 사용자`() {
            // given
            val accessToken = "access_token_123"
            val refreshToken = "refresh_token_456"
            val newUser = TestUser.createPending()

            val connection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            // OAuth 제공자 설정
            every { oAuthProviderFactory.getProvider(provider) } returns oAuthProvider
            every { oAuthProvider.getAccessToken(code) } returns socialToken
            every { oAuthProvider.getUserInfo(socialToken, false) } returns userInfo
            every { oAuthProvider.getProviderType() } returns Provider.GOOGLE

            // 사용자 조회/생성
            every { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) } returns null
            every { userRepository.findByEmail(email) } returns null
            every { userRepository.save(any<User>()) } returns newUser
            every { userConnectionRepository.save(any<UserConnection>()) } returns connection

            // JWT 토큰 생성
            every { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) } returns accessToken
            every { jwtTokenProvider.createRefreshToken(userId.toString()) } returns refreshToken

            // when
            val result = authService.login(provider, code)

            // then
            assertNotNull(result)
            assertEquals(newUser, result.user)
            assertEquals(accessToken, result.tokenPair.accessToken)
            assertEquals(refreshToken, result.tokenPair.refreshToken)

            verify(exactly = 1) { oAuthProviderFactory.getProvider(provider) }
            verify(exactly = 1) { oAuthProvider.getAccessToken(code) }
            verify(exactly = 1) { oAuthProvider.getUserInfo(socialToken, false) }
            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 1) { userRepository.findByEmail(email) }
            verify(exactly = 1) { userRepository.save(any<User>()) }
            verify(exactly = 1) { userConnectionRepository.save(any<UserConnection>()) }
            verify(exactly = 1) { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) }
            verify(exactly = 1) { jwtTokenProvider.createRefreshToken(userId.toString()) }
        }

        @Test
        fun `소셜 로그인 성공 - 기존 연결된 사용자`() {
            // given
            val accessToken = "access_token_123"
            val refreshToken = "refresh_token_456"
            val existingUser = TestUser.create()

            val connection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            // OAuth 제공자 설정
            every { oAuthProviderFactory.getProvider(provider) } returns oAuthProvider
            every { oAuthProvider.getAccessToken(code) } returns socialToken
            every { oAuthProvider.getUserInfo(socialToken, false) } returns userInfo
            every { oAuthProvider.getProviderType() } returns Provider.GOOGLE

            // 사용자 조회
            every {
                userConnectionRepository.findByProviderAndProviderId(
                    Provider.GOOGLE,
                    providerId
                )
            } returns connection
            every { userRepository.findById(userId) } returns Optional.of(existingUser)

            // JWT 토큰 생성
            every { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) } returns accessToken
            every { jwtTokenProvider.createRefreshToken(userId.toString()) } returns refreshToken

            // when
            val result = authService.login(provider, code)

            // then
            assertNotNull(result)
            assertEquals(existingUser, result.user)
            assertEquals(accessToken, result.tokenPair.accessToken)
            assertEquals(refreshToken, result.tokenPair.refreshToken)
            assertEquals(refreshToken, existingUser.refreshToken)

            verify(exactly = 1) { oAuthProviderFactory.getProvider(provider) }
            verify(exactly = 1) { oAuthProvider.getAccessToken(code) }
            verify(exactly = 1) { oAuthProvider.getUserInfo(socialToken, false) }
            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 0) { userRepository.findByEmail(any()) }
            verify(exactly = 0) { userRepository.save(any<User>()) }
            verify(exactly = 0) { userConnectionRepository.save(any<UserConnection>()) }
            verify(exactly = 1) { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) }
            verify(exactly = 1) { jwtTokenProvider.createRefreshToken(userId.toString()) }
        }

        @Test
        fun `소셜 로그인 성공 - 이메일이 같은 기존 사용자 (계정 통합)`() {
            // given
            val accessToken = "access_token_123"
            val refreshToken = "refresh_token_456"
            val existingUser = TestUser.create()

            val newConnection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            val tokenPair = TokenPair(
                accessToken = accessToken,
                refreshToken = refreshToken
            )

            // OAuth 제공자 설정
            every { oAuthProviderFactory.getProvider(provider) } returns oAuthProvider
            every { oAuthProvider.getAccessToken(code) } returns socialToken
            every { oAuthProvider.getUserInfo(socialToken, false) } returns userInfo
            every { oAuthProvider.getProviderType() } returns Provider.GOOGLE

            // 사용자 조회
            every { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) } returns null
            every { userRepository.findByEmail(email) } returns existingUser
            every { userConnectionRepository.save(any<UserConnection>()) } returns newConnection

            // JWT 토큰 생성
            every { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) } returns accessToken
            every { jwtTokenProvider.createRefreshToken(userId.toString()) } returns refreshToken

            // when
            val result = authService.login(provider, code)

            // then
            assertNotNull(result)
            assertEquals(existingUser, result.user)
            assertEquals(accessToken, result.tokenPair.accessToken)
            assertEquals(refreshToken, result.tokenPair.refreshToken)
            assertEquals(refreshToken, existingUser.refreshToken)

            verify(exactly = 1) { oAuthProviderFactory.getProvider(provider) }
            verify(exactly = 1) { oAuthProvider.getAccessToken(code) }
            verify(exactly = 1) { oAuthProvider.getUserInfo(socialToken, false) }
            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 1) { userRepository.findByEmail(email) }
            verify(exactly = 0) { userRepository.save(any<User>()) }
            verify(exactly = 1) { userConnectionRepository.save(any<UserConnection>()) }
            verify(exactly = 1) { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) }
            verify(exactly = 1) { jwtTokenProvider.createRefreshToken(userId.toString()) }
        }

        @Test
        fun `소셜 로그인 실패 - 유효하지 않은 제공자`() {
            // given
            val invalidProvider = "invalid"

            every { oAuthProviderFactory.getProvider(invalidProvider) } throws ApplicationException(ErrorCode.INVALID_PROVIDER)

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.login(invalidProvider, code)
            }

            assertEquals(ErrorCode.INVALID_PROVIDER, exception.errorCode)

            verify(exactly = 1) { oAuthProviderFactory.getProvider(invalidProvider) }
            verify(exactly = 0) { oAuthProvider.getAccessToken(any()) }
        }

        @Test
        fun `소셜 로그인 실패 - 비활성 사용자`() {
            // given
            val inactiveUser = TestUser.createInactive()

            val connection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            // OAuth 제공자 설정
            every { oAuthProviderFactory.getProvider(provider) } returns oAuthProvider
            every { oAuthProvider.getAccessToken(code) } returns socialToken
            every { oAuthProvider.getUserInfo(socialToken, false) } returns userInfo
            every { oAuthProvider.getProviderType() } returns Provider.GOOGLE

            // 사용자 조회
            every { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) } returns connection
            every { userRepository.findById(userId) } returns Optional.of(inactiveUser)

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.login(provider, code)
            }

            assertEquals(ErrorCode.INACTIVE_USER, exception.errorCode)

            verify(exactly = 1) { oAuthProviderFactory.getProvider(provider) }
            verify(exactly = 1) { oAuthProvider.getAccessToken(code) }
            verify(exactly = 1) { oAuthProvider.getUserInfo(socialToken, false) }
            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 0) { jwtTokenProvider.createAccessToken(any(), any()) }
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    inner class RefreshTokenTest {
        private val userId = TestUser.USER_ID
        private val refreshToken = "refresh_token_123"
        private val newAccessToken = "new_access_token_123"
        private val newRefreshToken = "new_refresh_token_456"

        @Test
        fun `토큰 갱신 성공`() {
            // given
            val existingUser = TestUser.createWithToken(refreshToken)

            // JWT 검증 및 토큰 생성
            every { jwtTokenProvider.validateRefreshToken(refreshToken) } returns true
            every { jwtTokenProvider.getUserId(refreshToken) } returns userId
            every { userRepository.findById(userId) } returns Optional.of(existingUser)
            every { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) } returns newAccessToken
            every { jwtTokenProvider.createRefreshToken(userId.toString()) } returns newRefreshToken

            // when
            val result = authService.refresh(refreshToken)

            // then
            assertNotNull(result)
            assertEquals(newAccessToken, result.accessToken)
            assertEquals(newRefreshToken, result.refreshToken)
            assertEquals(newRefreshToken, existingUser.refreshToken)

            verify(exactly = 1) { jwtTokenProvider.validateRefreshToken(refreshToken) }
            verify(exactly = 1) { jwtTokenProvider.getUserId(refreshToken) }
            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 1) { jwtTokenProvider.createAccessToken(userId.toString(), UserRole.NORMAL.name) }
            verify(exactly = 1) { jwtTokenProvider.createRefreshToken(userId.toString()) }
        }

        @Test
        fun `토큰 갱신 실패 - 유효하지 않은 리프레시 토큰`() {
            // given
            every { jwtTokenProvider.validateRefreshToken(refreshToken) } returns false

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.refresh(refreshToken)
            }

            assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.errorCode)

            verify(exactly = 1) { jwtTokenProvider.validateRefreshToken(refreshToken) }
            verify(exactly = 0) { jwtTokenProvider.getUserId(any()) }
            verify(exactly = 0) { userRepository.findById(any()) }
        }

        @Test
        fun `토큰 갱신 실패 - 사용자 없음`() {
            // given
            every { jwtTokenProvider.validateRefreshToken(refreshToken) } returns true
            every { jwtTokenProvider.getUserId(refreshToken) } returns userId
            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.refresh(refreshToken)
            }

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)

            verify(exactly = 1) { jwtTokenProvider.validateRefreshToken(refreshToken) }
            verify(exactly = 1) { jwtTokenProvider.getUserId(refreshToken) }
            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 0) { jwtTokenProvider.createAccessToken(any(), any()) }
        }

        @Test
        fun `토큰 갱신 실패 - 비활성 사용자`() {
            // given
            val inactiveUser = TestUser.createInactive()
            inactiveUser.updateRefreshToken(refreshToken)

            every { jwtTokenProvider.validateRefreshToken(refreshToken) } returns true
            every { jwtTokenProvider.getUserId(refreshToken) } returns userId
            every { userRepository.findById(userId) } returns Optional.of(inactiveUser)

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.refresh(refreshToken)
            }

            assertEquals(ErrorCode.INACTIVE_USER, exception.errorCode)

            verify(exactly = 1) { jwtTokenProvider.validateRefreshToken(refreshToken) }
            verify(exactly = 1) { jwtTokenProvider.getUserId(refreshToken) }
            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 0) { jwtTokenProvider.createAccessToken(any(), any()) }
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    inner class LogoutTest {
        private val userId = TestUser.USER_ID

        @Test
        fun `로그아웃 성공`() {
            // given
            val existingUser = TestUser.createWithToken()

            every { userRepository.findById(userId) } returns Optional.of(existingUser)

            // when
            authService.logout(userId)

            // then
            assertNull(existingUser.refreshToken)

            verify(exactly = 1) { userRepository.findById(userId) }
        }

        @Test
        fun `로그아웃 실패 - 사용자 없음`() {
            // given
            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.logout(userId)
            }

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)

            verify(exactly = 1) { userRepository.findById(userId) }
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트")
    inner class WithdrawTest {
        private val userId = TestUser.USER_ID

        @Test
        fun `회원 탈퇴 성공`() {
            // given
            val existingUser = TestUser.createWithToken()

            every { userRepository.findById(userId) } returns Optional.of(existingUser)

            // when
            authService.withdraw(userId)

            // then
            assertEquals(UserStatus.INACTIVE, existingUser.status)
            assertNull(existingUser.refreshToken)

            verify(exactly = 1) { userRepository.findById(userId) }
        }

        @Test
        fun `회원 탈퇴 실패 - 사용자 없음`() {
            // given
            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.withdraw(userId)
            }

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)

            verify(exactly = 1) { userRepository.findById(userId) }
        }
    }

    @Nested
    @DisplayName("사용자 조회 또는 생성 테스트")
    inner class FindOrCreateUserTest {
        private val userId = TestUser.USER_ID
        private val email = "heejin@test.com"
        private val name = "전희진"
        private val providerId = "google_user_id_123"
        private val socialToken = "social_token_123"

        @Test
        fun `기존 연결된 사용자 조회 성공`() {
            // given
            val existingUser = TestUser.create()

            val connection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            every {
                userConnectionRepository.findByProviderAndProviderId(
                    Provider.GOOGLE,
                    providerId
                )
            } returns connection
            every { userRepository.findById(userId) } returns Optional.of(existingUser)

            // when
            val (user, isNewUser) = authService.findOrCreateUser(Provider.GOOGLE, providerId, email, name, socialToken)

            // then
            assertNotNull(user)
            assertEquals(existingUser, user)
            assertEquals(socialToken, user.refreshToken)
            assertFalse(isNewUser)

            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 0) { userRepository.findByEmail(any()) }
            verify(exactly = 0) { userRepository.save(any<User>()) }
        }

        @Test
        fun `이메일이 같은 기존 사용자 조회 성공 (계정 통합)`() {
            // given
            val existingUser = TestUser.create()

            val newConnection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            every { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) } returns null
            every { userRepository.findByEmail(email) } returns existingUser
            every { userConnectionRepository.save(any<UserConnection>()) } returns newConnection

            // when
            val (user, isNewUser) = authService.findOrCreateUser(Provider.GOOGLE, providerId, email, name, socialToken)

            // then
            assertNotNull(user)
            assertEquals(existingUser, user)
            assertEquals(socialToken, user.refreshToken)
            assertFalse(isNewUser)

            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 0) { userRepository.findById(any()) }
            verify(exactly = 1) { userRepository.findByEmail(email) }
            verify(exactly = 0) { userRepository.save(any<User>()) }
            verify(exactly = 1) { userConnectionRepository.save(any<UserConnection>()) }
        }

        @Test
        fun `신규 사용자 생성 성공`() {
            // given
            val newUser = TestUser.createPending()
            newUser.updateRefreshToken(socialToken)

            val newConnection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            every { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) } returns null
            every { userRepository.findByEmail(email) } returns null
            every { userRepository.save(any<User>()) } returns newUser
            every { userConnectionRepository.save(any<UserConnection>()) } returns newConnection

            // when
            val (user, isNewUser) = authService.findOrCreateUser(Provider.GOOGLE, providerId, email, name, socialToken)

            // then
            assertNotNull(user)
            assertEquals(newUser, user)
            assertEquals(socialToken, user.refreshToken)
            assertTrue(isNewUser)

            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 0) { userRepository.findById(any()) }
            verify(exactly = 1) { userRepository.findByEmail(email) }
            verify(exactly = 1) { userRepository.save(any<User>()) }
            verify(exactly = 1) { userConnectionRepository.save(any<UserConnection>()) }
        }

        @Test
        fun `비활성 사용자 조회 시 예외 발생`() {
            // given
            val inactiveUser = TestUser.createInactive()

            val connection = UserConnection(
                userId = userId,
                provider = Provider.GOOGLE,
                providerId = providerId
            )

            every {
                userConnectionRepository.findByProviderAndProviderId(
                    Provider.GOOGLE,
                    providerId
                )
            } returns connection
            every { userRepository.findById(userId) } returns Optional.of(inactiveUser)

            // when & then
            val exception = assertThrows(ApplicationException::class.java) {
                authService.findOrCreateUser(Provider.GOOGLE, providerId, email, name, socialToken)
            }

            assertEquals(ErrorCode.INACTIVE_USER, exception.errorCode)

            verify(exactly = 1) { userConnectionRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId) }
            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 0) { userRepository.findByEmail(any()) }
            verify(exactly = 0) { userRepository.save(any<User>()) }
        }
    }
}
