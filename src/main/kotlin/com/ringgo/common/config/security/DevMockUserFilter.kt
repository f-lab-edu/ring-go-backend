package com.ringgo.common.config.security

import com.ringgo.domain.user.entity.User
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Profile("local")
class DevMockUserFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val mockUser = User(
                id = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a"),
                name = "전희진",
                email = "heejin@test.com",
                provider = "KAKAO",
                providerId = "kakao_123"
            )
            val authentication = UsernamePasswordAuthenticationToken(mockUser, null, emptyList())
            SecurityContextHolder.getContext().authentication = authentication
        }

        chain.doFilter(request, response)
    }
}