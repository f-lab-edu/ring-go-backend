package com.ringgo.domain.meeting.entity

import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "meeting")
class Meeting(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),  // AUTO_INCREMENT 대신 UUID 사용

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false)
    var icon: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MeetingStatus = MeetingStatus.ACTIVE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateStatus(newStatus: MeetingStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw BusinessException(ErrorCode.INVALID_STATUS_TRANSITION)
        }
        this.status = newStatus
        this.updatedAt = LocalDateTime.now()
    }

    fun validateCreator(userId: UUID) {
        if (creator.id != userId) {
            throw BusinessException(ErrorCode.NOT_MEETING_CREATOR)
        }
    }
}