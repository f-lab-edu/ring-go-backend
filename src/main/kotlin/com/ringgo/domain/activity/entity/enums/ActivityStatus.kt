package com.ringgo.domain.activity.entity.enums

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging

enum class ActivityStatus {
    ACTIVE, ENDED, DELETED;

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun validateTransitionTo(newStatus: ActivityStatus) {
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