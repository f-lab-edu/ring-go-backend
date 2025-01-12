package com.ringgo.domain.meeting.repository

import com.ringgo.domain.meeting.dto.MeetingWithMemberCount
import java.util.UUID

interface MeetingRepositoryCustom {
    fun findMeetingWithMemberCount(userId: UUID): List<MeetingWithMemberCount>
}