package com.ringgo.common.config.security.oauth.core

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * OAuth 상태 토큰 관리 클래스
 */
@Component
class OAuthStateStore {
    // 상태 토큰 저장소: key=providerName:state, value=(state, expiryTime)
    private val stateMap = ConcurrentHashMap<String, Pair<String, Long>>()

    /**
     * 상태 토큰 저장
     */
    fun saveState(provider: String, state: String, timeout: Long, unit: TimeUnit) {
        val expiry = System.currentTimeMillis() + unit.toMillis(timeout)
        stateMap["$provider:$state"] = state to expiry

        // 만료된 상태 토큰 정리
        cleanExpiredStates()
    }

    /**
     * 상태 토큰 검증
     */
    fun validateState(provider: String, state: String): Boolean {
        val key = "$provider:$state"
        val storedData = stateMap.remove(key) ?: return false
        val (storedState, expiry) = storedData

        return storedState == state && expiry > System.currentTimeMillis()
    }

    /**
     * 만료된 상태 토큰 정리
     */
    private fun cleanExpiredStates() {
        val now = System.currentTimeMillis()
        stateMap.entries.removeIf { (_, value) -> value.second < now }
    }
}
