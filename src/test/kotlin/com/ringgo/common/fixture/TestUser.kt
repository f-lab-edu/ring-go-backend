package com.ringgo.common.fixture

import com.ringgo.domain.user.entity.User
import java.util.UUID

object TestUser {
    fun create() = User(
        id = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a"),
        name = "전희진",
        email = "heejin@test.com",
        provider = "KAKAO",
        providerId = "kakao_123"
    )
}