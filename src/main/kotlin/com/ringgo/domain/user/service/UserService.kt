package com.ringgo.domain.user.service

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.user.entity.User
import com.ringgo.domain.user.entity.UserProfile
import com.ringgo.domain.user.entity.enums.UserStatus
import com.ringgo.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun completeSignup(userId: UUID, nickname: String, profileImage: String?): User {
        log.debug { "회원가입 완료 진행 - 사용자 ID: $userId, 닉네임: $nickname" }

        val user = userRepository.findById(userId).orElseThrow {
            log.warn { "회원가입 완료 중 사용자를 찾을 수 없음 - 사용자 ID: $userId" }
            ApplicationException(ErrorCode.USER_NOT_FOUND)
        }

        // 사용자 상태 확인
        if (user.status == UserStatus.INACTIVE) {
            log.warn { "비활성 사용자의 회원가입 완료 시도 - 사용자 ID: $userId" }
            throw ApplicationException(ErrorCode.INACTIVE_USER)
        }

        // 프로필이 없으면 새로 생성, 있으면 속성만 업데이트 (더티 체킹 활용)
        if (user.profile == null) {
            log.debug { "새 프로필 생성 - 사용자 ID: $userId" }
            user.profile = UserProfile(
                userId = user.id,
                nickname = nickname,
                imagePath = profileImage,
                user = user
            )
        } else {
            log.debug { "기존 프로필 업데이트 - 사용자 ID: $userId" }
            val profile = user.profile!!
            profile.nickname = nickname
            profile.imagePath = profileImage
        }

        // 사용자 상태 ACTIVE로 변경
        if (user.status == UserStatus.PENDING) {
            user.activate()
        }

        log.info { "회원가입 완료됨 - 사용자 ID: $userId" }
        return user
    }
}
