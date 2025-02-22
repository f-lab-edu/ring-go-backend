package com.ringgo.domain.activity.entity.enums

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

enum class ActivityStatus {
    ACTIVE, ENDED, DELETED;

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