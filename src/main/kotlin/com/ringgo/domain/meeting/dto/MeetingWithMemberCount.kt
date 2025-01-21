package com.ringgo.domain.meeting.dto

import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.user.entity.User
import java.time.LocalDateTime
import java.util.UUID

data class MeetingWithMemberCount(
    val id: UUID,
    val name: String,
    val icon: String,
    val status: MeetingStatus,
    val memberCount: Long,
    val creator: User,
    val createdAt: LocalDateTime
)