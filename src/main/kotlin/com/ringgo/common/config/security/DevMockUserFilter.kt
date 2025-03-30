package com.ringgo.common.config.security

import com.ringgo.domain.user.entity.User
import com.ringgo.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * 개발 환경에서만 활성화되는 모의 인증 필터
 * 실제 인증 없이 테스트할 때 사용
 */
@Component
@Profile("local", "dev") // 로컬, 개발 환경에서만 활성화
class DevMockUserFilter(
    private val userRepository: UserRepository,
    @Value("\${app.auth.mock-enabled:false}") private val mockEnabled: Boolean,
    @Value("\${app.auth.mock-user-id:}") private val mockUserIdStr: String
) : OncePerRequestFilter() {

    private val mockUserId: UUID? = try {
        if (mockUserIdStr.isNotBlank()) UUID.fromString(mockUserIdStr) else null
    } catch (e: Exception) {
        log.warn { "유효하지 않은 모의 사용자 ID: $mockUserIdStr" }
        null
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: jakarta.servlet.http.HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 이미 인증이 있거나 mock이 비활성화되어 있으면 통과
        if (!mockEnabled || SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 테스트용 헤더가 있는지 확인 (Postman 등에서 헤더 추가 시 활성화)
            val useMock = request.getHeader("X-USE-MOCK-USER") == "true"

            // mockEnabled가 true이고 헤더가 있거나, mockUserId가 설정되어 있는 경우에만 적용
            if (useMock || mockUserId != null) {
                val user = findMockUser()

                if (user != null) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                    val auth = UsernamePasswordAuthenticationToken(user, null, authorities)
                    SecurityContextHolder.getContext().authentication = auth

                    log.debug { "DevMockUserFilter: 테스트 사용자로 인증됨 - ${user.id}" }
                }
            }
        } catch (e: Exception) {
            log.error(e) { "DevMockUserFilter 오류" }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * 모의 사용자 찾기
     * 1. 설정된 mockUserId로 찾기
     * 2. 없으면 기본 테스트 사용자 ID로 찾기
     * 3. 없으면 첫 번째 사용자 반환
     */
    private fun findMockUser(): User? {
        // 1. 설정된 ID로 찾기
        if (mockUserId != null) {
            userRepository.findById(mockUserId).orElse(null)?.let { return it }
        }

        // 2. 기본 테스트 사용자 ID로 찾기
        val defaultTestUserId = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")
        userRepository.findById(defaultTestUserId).orElse(null)?.let { return it }

        // 3. 첫 번째 사용자 반환
        return userRepository.findAll().firstOrNull()
    }
}
