package com.ringgo.domain.member.repository

import com.ringgo.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom {
    fun countByMeetingId(meetingId: UUID): Int
    fun existsByMeetingIdAndUserId(meetingId: UUID, userId: UUID): Boolean
    fun findByMeetingIdAndUserId(meetingId: UUID, userId: UUID): Member?
}