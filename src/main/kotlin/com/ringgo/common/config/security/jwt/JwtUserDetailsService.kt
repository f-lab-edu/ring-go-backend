package com.ringgo.common.config.security.jwt

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * Spring Security 인증 과정에서 사용자 ID를 기반으로 사용자 정보를 로드하는 서비스
 */
@Service
class JwtUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    /**
     * 사용자 ID(문자열)를 받아 해당 사용자 엔티티를 로드
     */
    override fun loadUserByUsername(userId: String): UserDetails {
        log.debug { "사용자 상세 정보 로드 - ID: $userId" }

        try {
            // UUID로 변환
            val uuid = UUID.fromString(userId)

            // 사용자 엔티티 조회
            val user = userRepository.findById(uuid)
                .orElseThrow { ApplicationException(ErrorCode.USER_NOT_FOUND) }

            // UserDetails 구현체로 변환하여 반환
            return JwtUserDetails(user)
        } catch (e: IllegalArgumentException) {
            log.error { "유효하지 않은 사용자 ID 형식: $userId" }
            throw ApplicationException(ErrorCode.USER_NOT_FOUND)
        } catch (e: Exception) {
            log.error(e) { "사용자 정보 로드 중 오류 - ID: $userId" }
            throw ApplicationException(ErrorCode.USER_NOT_FOUND)
        }
    }
}
