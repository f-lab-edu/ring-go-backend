package com.ringgo.domain.meeting.config

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.meeting.invite")
data class MeetingInviteConfig(
    val baseUrl: String,
    var inviteExpirationDays: Long = 3,
    var maxMembers: Int = 5,
) {
    init {
        if (baseUrl.isBlank()) throw ApplicationException(ErrorCode.INVALID_BASE_URL)
    }
}