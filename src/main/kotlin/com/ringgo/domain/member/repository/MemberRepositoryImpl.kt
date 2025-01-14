package com.ringgo.domain.member.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.ringgo.domain.meeting.dto.MeetingWithMemberCount
import com.ringgo.domain.meeting.entity.QMeeting.*
import com.ringgo.domain.member.entity.QMember.*
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MemberRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MemberRepositoryCustom {
    override fun findMeetingWithMemberCount(userId: UUID): List<MeetingWithMemberCount> {
        val member = member
        val meeting = meeting

        return queryFactory
            .select(
                Projections.constructor(
                    MeetingWithMemberCount::class.java,
                    meeting.id,
                    meeting.name,
                    meeting.icon,
                    meeting.status,
                    member.count().intValue(),
                    meeting.creator,
                    meeting.createdAt
                )
            )
            .from(member)
            .join(member.meeting, meeting)
            .where(member.user.id.eq(userId))
            .groupBy(
                meeting.id,
                meeting.name,
                meeting.icon,
                meeting.status,
                meeting.creator,
                meeting.createdAt
            )
            .fetch()
    }
}