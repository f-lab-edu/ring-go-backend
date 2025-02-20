package com.ringgo.domain.member.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.ringgo.domain.meeting.dto.MeetingWithMemberCount
import com.ringgo.domain.meeting.entity.QMeeting
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.member.entity.QMember
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MemberRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MemberRepositoryCustom {
    override fun findMeetingWithMemberCount(userId: UUID): List<MeetingWithMemberCount> {
        val meeting = QMeeting.meeting
        val member = QMember.member

        // 1. 사용자가 속한 모임 정보 조회
        val meetings = queryFactory
            .select(meeting)
            .from(meeting)
            .join(member).on(member.meeting.eq(meeting))
            .where(
                member.user.id.eq(userId),
                meeting.status.ne(MeetingStatus.DELETED)
            )
            .fetch()

        // 2. 각 모임의 멤버 수 조회
        val memberCounts = queryFactory
            .select(
                member.meeting.id,
                member.count()
            )
            .from(member)
            .where(member.meeting.id.`in`(meetings.map { it.id }))
            .groupBy(member.meeting.id)
            .fetch()
            .associate { tuple ->
                tuple.get(member.meeting.id) to tuple.get(member.count())
            }

        // 3. 애플리케이션에서 데이터 조합
        return meetings.map { meeting ->
            MeetingWithMemberCount(
                id = meeting.id,
                name = meeting.name,
                icon = meeting.icon,
                status = meeting.status,
                memberCount = memberCounts[meeting.id] ?: 0,
                creator = meeting.creator,
                createdAt = meeting.createdAt,
            )
        }
    }
}