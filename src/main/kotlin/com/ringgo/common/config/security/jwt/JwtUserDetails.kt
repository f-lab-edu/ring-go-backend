package com.ringgo.common.config.security.jwt

import com.ringgo.domain.user.entity.User
import com.ringgo.domain.user.entity.enums.UserStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security에서 사용할 사용자 정보 래퍼 클래스
 */
class JwtUserDetails(val user: User) : UserDetails {

    /**
     * 사용자의 권한 목록 반환
     */
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }

    /**
     * 비밀번호 반환 (소셜 로그인이므로 사용하지 않음)
     */
    override fun getPassword(): String? = null

    /**
     * 사용자명 반환 (사용자 ID 사용)
     */
    override fun getUsername(): String = user.id.toString()

    /**
     * 계정 만료 여부
     */
    override fun isAccountNonExpired(): Boolean = true

    /**
     * 계정 잠금 여부
     */
    override fun isAccountNonLocked(): Boolean = true

    /**
     * 자격 증명 만료 여부
     */
    override fun isCredentialsNonExpired(): Boolean = true

    /**
     * 계정 활성화 여부
     */
    override fun isEnabled(): Boolean = user.status == UserStatus.ACTIVE
}
