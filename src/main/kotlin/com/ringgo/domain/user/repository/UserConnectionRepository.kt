package com.ringgo.domain.user.repository

import com.ringgo.domain.user.entity.UserConnection
import com.ringgo.domain.user.entity.enums.Provider
import org.springframework.data.jpa.repository.JpaRepository

interface UserConnectionRepository : JpaRepository<UserConnection, Long> {
    fun findByProviderAndProviderId(provider: Provider, providerId: String): UserConnection?
}
