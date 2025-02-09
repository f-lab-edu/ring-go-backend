package com.ringgo.domain.meeting.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MeetingInviteRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MeetingInviteRepositoryCustom {

}