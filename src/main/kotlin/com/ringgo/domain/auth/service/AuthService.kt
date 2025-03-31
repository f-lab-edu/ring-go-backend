package com.ringgo.domain.auth.service

import com.ringgo.common.config.security.jwt.JwtTokenProvider
import com.ringgo.common.config.security.jwt.TokenPair
import com.ringgo.common.config.security.oauth.core.OAuthProviderFactory
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.auth.dto.LoginResult
import com.ringgo.domain.user.entity.User
import com.ringgo.domain.user.entity.UserConnection
import com.ringgo.domain.user.entity.enums.Provider
import com.ringgo.domain.user.entity.enums.UserRole
import com.ringgo.domain.user.entity.enums.UserStatus
import com.ringgo.domain.user.repository.UserConnectionRepository
import com.ringgo.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userConnectionRepository: UserConnectionRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthProviderFactory: OAuthProviderFactory
) {
    @Transactional
    fun login(provider: String, code: String): LoginResult {
        log.info { "소셜 로그인 시작 - 제공자: $provider" }

        try {
            // 1. 소셜 로그인 제공자 선택
            val oauthProvider = oAuthProviderFactory.getProvider(provider)

            // 2. 제공자 enum 파싱
            val providerEnum = try {
                Provider.valueOf(provider.uppercase())
            } catch (e: IllegalArgumentException) {
                log.error { "유효하지 않은 제공자: $provider" }
                throw ApplicationException(ErrorCode.INVALID_PROVIDER)
            }

            // 3. 소셜 로그인 액세스 토큰 획득
            log.debug { "소셜 액세스 토큰 획득 중 - 제공자: $providerEnum" }
            val socialToken = oauthProvider.getAccessToken(code)

            // 4. 소셜 유저 정보 획득 (프로필 이미지 제외)
            log.debug { "소셜 사용자 정보 획득 중 - 제공자: $providerEnum" }
            val socialUserInfo = oauthProvider.getUserInfo(socialToken, includeProfileImage = false)

            // 5. 사용자 조회 또는 생성 (계정 통합 로직 적용)
            val (user, isNewUser) = findOrCreateUser(providerEnum, socialUserInfo.providerId, socialUserInfo.email, socialUserInfo.name, socialToken)

            // 6. JWT 토큰 발급
            val accessToken = jwtTokenProvider.createAccessToken(user.id.toString(), user.role.name)
            val refreshToken = jwtTokenProvider.createRefreshToken(user.id.toString())

            // 7. 리프레시 토큰 업데이트
            user.updateRefreshToken(refreshToken)

            log.info { "소셜 로그인 성공 - 사용자 ID: ${user.id}, 제공자: $providerEnum, 신규 사용자: $isNewUser" }
            return LoginResult(
                user = user,
                tokenPair = TokenPair(accessToken = accessToken, refreshToken = refreshToken)
            )
        } catch (e: ApplicationException) {
            // ApplicationException은 그대로 전달 (INVALID_PROVIDER, INACTIVE_USER 등)
            log.error { "소셜 로그인 실패 - 제공자: $provider, 메시지: ${e.message}" }
            throw e
        } catch (e: Exception) {
            // 기타 예외는 INTERNAL_SERVER_ERROR로 변환
            log.error { "소셜 로그인 실패 - 제공자: $provider, 메시지: ${e.message}" }
            e.printStackTrace()
            throw ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * 사용자 조회 또는 생성
     */
    @Transactional
    fun findOrCreateUser(
        provider: Provider,
        providerId: String,
        email: String,
        name: String,
        socialToken: String
    ): Pair<User, Boolean> {
        // 1. 소셜 로그인 연결 정보로 사용자 조회
        val connection = userConnectionRepository.findByProviderAndProviderId(provider, providerId)

        if (connection != null) {
            // 기존 소셜 로그인 연결이 있는 경우
            val user = userRepository.findById(connection.userId).orElse(null)
                ?: run {
                    log.error { "연결에 해당하는 사용자를 찾을 수 없음 - 사용자 ID: ${connection.userId}" }
                    throw ApplicationException(ErrorCode.USER_NOT_FOUND)
                }

            if (user.status == UserStatus.INACTIVE) {
                throw ApplicationException(ErrorCode.INACTIVE_USER)
            }

            // 토큰 업데이트
            user.updateRefreshToken(socialToken)
            return user to false
        }

        // 2. 이메일로 사용자 조회 (계정 통합)
        val existingUser = userRepository.findByEmail(email)

        if (existingUser != null) {
            // 이메일이 일치하는 사용자가 있는 경우 (다른 소셜 로그인으로 가입한 사용자)
            if (existingUser.status == UserStatus.INACTIVE) {
                throw ApplicationException(ErrorCode.INACTIVE_USER)
            }

            // 소셜 로그인 연결 정보 추가
            val newConnection = UserConnection(
                userId = existingUser.id,
                provider = provider,
                providerId = providerId
            )
            userConnectionRepository.save(newConnection)

            // 토큰 업데이트
            existingUser.updateRefreshToken(socialToken)
            log.info { "기존 계정에 새 제공자 연결됨 - 이메일: $email, 제공자: $provider" }
            return existingUser to false
        }

        // 3. 새 사용자 생성
        log.debug { "새 사용자 생성 중 - 제공자: $provider, 이메일: $email" }
        val newUser = createUser(email, name, socialToken)

        // 소셜 로그인 연결 정보 추가
        val newConnection = UserConnection(
            userId = newUser.id,
            provider = provider,
            providerId = providerId
        )
        userConnectionRepository.save(newConnection)

        return newUser to true
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        log.debug { "토큰 갱신 요청됨" }
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn { "유효하지 않은 리프레시 토큰" }
            throw ApplicationException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val user = userRepository.findById(userId).orElse(null)
            ?: run {
                log.warn { "리프레시 토큰에 해당하는 사용자를 찾을 수 없음 - 사용자 ID: $userId" }
                throw ApplicationException(ErrorCode.USER_NOT_FOUND)
            }

        if (user.status == UserStatus.INACTIVE) {
            log.warn { "비활성 사용자가 토큰 갱신 시도 - 사용자 ID: $userId" }
            throw ApplicationException(ErrorCode.INACTIVE_USER)
        }

        val accessToken = jwtTokenProvider.createAccessToken(user.id.toString(), user.role.name)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(user.id.toString())

        user.updateRefreshToken(newRefreshToken)

        log.info { "토큰 갱신 성공 - 사용자 ID: $userId" }
        return TokenPair(accessToken = accessToken, refreshToken = newRefreshToken)
    }

    @Transactional
    fun logout(userId: UUID) {
        log.debug { "로그아웃 요청됨 - 사용자 ID: $userId" }
        val user = userRepository.findById(userId).orElse(null)
            ?: run {
                log.warn { "로그아웃을 위한 사용자를 찾을 수 없음 - 사용자 ID: $userId" }
                throw ApplicationException(ErrorCode.USER_NOT_FOUND)
            }

        user.updateRefreshToken(null)
        log.info { "로그아웃 성공 - 사용자 ID: $userId" }
    }

    @Transactional
    fun withdraw(userId: UUID) {
        log.debug { "회원 탈퇴 요청됨 - 사용자 ID: $userId" }
        val user = userRepository.findById(userId).orElse(null)
            ?: run {
                log.warn { "회원 탈퇴를 위한 사용자를 찾을 수 없음 - 사용자 ID: $userId" }
                throw ApplicationException(ErrorCode.USER_NOT_FOUND)
            }

        user.deactivate()
        user.updateRefreshToken(null)
        log.info { "회원 탈퇴 성공 - 사용자 ID: $userId" }
    }

    // 사용자 생성
    private fun createUser(
        email: String,
        name: String,
        refreshToken: String
    ): User {
        val user = User(
            email = email,
            name = name,
            refreshToken = refreshToken,
            role = UserRole.NORMAL,
            status = UserStatus.PENDING  // 추가 정보 입력 전에는 PENDING 상태
        )

        return userRepository.save(user)
    }
}
