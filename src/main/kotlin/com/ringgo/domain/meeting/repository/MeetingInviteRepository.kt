package com.ringgo.domain.meeting.repository

import com.ringgo.domain.meeting.entity.MeetingInvite
import org.springframework.data.jpa.repository.JpaRepository

interface MeetingInviteRepository : JpaRepository<MeetingInvite, Long>, MeetingInviteRepositoryCustom {
    fun findByCode(code: String): MeetingInvite?
}