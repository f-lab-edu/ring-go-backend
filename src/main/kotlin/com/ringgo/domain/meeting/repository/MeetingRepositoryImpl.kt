package com.ringgo.domain.meeting.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.ringgo.domain.meeting.dto.MeetingWithMemberCount
import com.ringgo.domain.meeting.entity.QMeeting
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.member.entity.QMember
import org.springframework.stereotype.Repository
import java.util.*


@Repository
class MeetingRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MeetingRepositoryCustom {
    override fun findMeetingWithMemberCount(userId: UUID): List<MeetingWithMemberCount> {
        val meeting = QMeeting.meeting
        val member = QMember.member

        return queryFactory
            .select(
                Projections.constructor(
                    MeetingWithMemberCount::class.java,
                    meeting.id,
                    meeting.name,
                    meeting.icon,
                    meeting.status,
                    member.count(),
                    meeting.creator,
                    meeting.createdAt
                )
            )
            .from(meeting)
            .join(member).on(member.meeting.eq(meeting))
            .where(
                member.user.id.eq(userId),
                meeting.status.ne(MeetingStatus.DELETED)
            )
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