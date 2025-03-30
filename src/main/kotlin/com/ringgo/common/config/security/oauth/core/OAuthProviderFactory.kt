package com.ringgo.common.config.security.oauth.core

import com.ringgo.common.config.security.oauth.api.OAuthProvider
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.user.entity.enums.Provider
import org.springframework.stereotype.Component

/**
 * OAuth 공급자 팩토리
 * 요청된 공급자 유형에 따라 OAuthProvider 구현체 반환
 */
@Component
class OAuthProviderFactory(
    private val providers: List<OAuthProvider>
) {
    // 공급자 유형별 맵 초기화
    private val providerMap: Map<Provider, OAuthProvider> = providers.associateBy { it.getProviderType() }

    /**
     * 공급자 구현체 조회
     */
    fun getProvider(providerType: String): OAuthProvider {
        val type = try {
            Provider.valueOf(providerType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ApplicationException(ErrorCode.INVALID_PROVIDER)
        }

        return providerMap[type] ?: throw ApplicationException(ErrorCode.INVALID_PROVIDER)
    }
}
