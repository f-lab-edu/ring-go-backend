package com.ringgo.domain.meeting.repository

import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MeetingRepository : JpaRepository<Meeting, UUID>, MeetingRepositoryCustom {

}