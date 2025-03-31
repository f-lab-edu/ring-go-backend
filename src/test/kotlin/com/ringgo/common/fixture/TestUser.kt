package com.ringgo.common.fixture

import com.ringgo.domain.user.entity.User
import com.ringgo.domain.user.entity.UserProfile
import com.ringgo.domain.user.entity.enums.UserRole
import com.ringgo.domain.user.entity.enums.UserStatus
import java.util.*

object TestUser {
    val USER_ID = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a")

    fun create() = User(
        id = USER_ID,
        email = "heejin@test.com",
        name = "전희진",
        refreshToken = null,
        role = UserRole.NORMAL,
        status = UserStatus.ACTIVE
    )

    fun createWithProfile(): User {
        val user = create()
        user.profile = UserProfile(
            userId = USER_ID,
            nickname = "희진",
            imagePath = "profile.jpg",
            user = user
        )
        return user
    }

    fun createWithToken(refreshToken: String = "test_refresh_token"): User {
        val user = create()
        user.updateRefreshToken(refreshToken)
        return user
    }

    fun createPending(): User {
        return User(
            id = USER_ID,
            email = "heejin@test.com",
            name = "전희진",
            refreshToken = null,
            role = UserRole.NORMAL,
            status = UserStatus.PENDING
        )
    }

    fun createInactive(): User {
        return User(
            id = USER_ID,
            email = "heejin@test.com",
            name = "전희진",
            refreshToken = null,
            role = UserRole.NORMAL,
            status = UserStatus.INACTIVE
        )
    }
}
