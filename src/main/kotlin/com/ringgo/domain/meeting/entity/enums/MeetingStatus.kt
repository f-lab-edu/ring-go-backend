package com.ringgo.domain.meeting.entity.enums

import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging

enum class MeetingStatus {
    ACTIVE, ENDED, DELETED;

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun validateTransitionTo(newStatus: MeetingStatus) {
        val isValid = when (this) {
            ACTIVE -> newStatus == ENDED || newStatus == DELETED
            ENDED -> newStatus == ACTIVE
            DELETED -> false
        }

        if (!isValid) {
            log.warn { "Invalid status transition - from: $this, to: $newStatus" }
            throw BusinessException(ErrorCode.INVALID_STATUS_TRANSITION)
        }
    }
}