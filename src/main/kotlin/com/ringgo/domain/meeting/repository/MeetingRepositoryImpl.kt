package com.ringgo.domain.meeting.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MeetingRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MeetingRepositoryCustom {

}