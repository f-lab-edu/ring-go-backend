package com.ringgo.domain.member.repository

import com.ringgo.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberRepository : JpaRepository<Member, UUID>, MemberRepositoryCustom {
    fun findByMeetingIdAndUserId(meetingId: UUID, userId: UUID): Member?
    fun countByMeetingId(meetingId: UUID): Long
    fun existsByMeetingIdAndUserId(meetingId: UUID, userId: UUID): Boolean
    fun findByMeetingIdOrderByJoinedAtAsc(meetingId: UUID): List<Member>
}