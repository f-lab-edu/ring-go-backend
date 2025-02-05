package com.ringgo.domain.meeting.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.meeting.invite")
data class MeetingInviteConfig(
    val baseUrl: String,
    var expirationDays: Long = 7,
    var maxRetries: Int = 3
) {
    init {
        require(baseUrl.isNotBlank()) { "app.meeting.invite.base-url must be configured" }
    }
}