package com.ringgo.domain.meeting.entity.enums

enum class MeetingStatus {
    ACTIVE, ENDED, DELETED;

    fun canTransitionTo(newStatus: MeetingStatus): Boolean {
        return when (this) {
            ACTIVE -> newStatus == ENDED || newStatus == DELETED
            ENDED -> newStatus == ACTIVE  // 종료 취소의 경우 ACTIVE로 돌아감
            DELETED -> false  // DELETED 상태에서는 다른 상태로 전환 불가
        }
    }
}