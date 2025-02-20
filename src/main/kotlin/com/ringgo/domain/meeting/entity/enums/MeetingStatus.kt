package com.ringgo.domain.meeting.entity.enums

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

enum class MeetingStatus {
    ACTIVE, ENDED, DELETED;

    fun validateTransitionTo(newStatus: MeetingStatus) {
        val isValid = when (this) {
            ACTIVE -> newStatus == ENDED || newStatus == DELETED
            ENDED -> newStatus == ACTIVE
            DELETED -> false
        }

        if (!isValid) {
            log.warn { "Invalid status transition - from: $this, to: $newStatus" }
            throw ApplicationException(ErrorCode.INVALID_STATUS_TRANSITION)
        }
    }
}