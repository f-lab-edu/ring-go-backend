package com.ringgo.domain.member.repository

import com.ringgo.domain.meeting.dto.MeetingWithMemberCount
import java.util.UUID

interface MemberRepositoryCustom {
    fun findMeetingWithMemberCount(userId: UUID): List<MeetingWithMemberCount>
}