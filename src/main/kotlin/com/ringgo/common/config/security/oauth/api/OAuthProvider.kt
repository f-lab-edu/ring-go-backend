package com.ringgo.common.config.security.oauth.api

import com.ringgo.domain.auth.dto.UserInfo
import com.ringgo.domain.user.entity.enums.Provider
import jakarta.servlet.http.HttpServletRequest

/**
 * OAuth 공급자 인터페이스
 */
interface OAuthProvider {
    /**
     * 인증 URL 생성
     */
    fun getAuthorizationUrl(redirectUri: String): String

    /**
     * 액세스 토큰 획득
     */
    fun getAccessToken(code: String): String

    /**
     * 사용자 정보 획득
     */
    fun getUserInfo(accessToken: String, includeProfileImage: Boolean = false): UserInfo

    /**
     * 콜백 검증
     */
    fun validateCallback(params: Map<String, String>, request: HttpServletRequest): Boolean

    /**
     * 제공자 유형 반환
     */
    fun getProviderType(): Provider
}
