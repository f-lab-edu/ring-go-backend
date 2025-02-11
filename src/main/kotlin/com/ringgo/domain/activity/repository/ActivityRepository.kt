package com.ringgo.domain.activity.repository

import com.ringgo.domain.activity.entity.Activity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ActivityRepository : JpaRepository<Activity, Long> {
    fun existsByMeetingIdAndType(meetingId: UUID, type: String): Boolean
}