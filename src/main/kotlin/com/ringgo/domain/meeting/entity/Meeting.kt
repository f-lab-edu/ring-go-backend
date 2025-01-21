package com.ringgo.domain.meeting.entity

import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "meeting")
@EntityListeners(AuditingEntityListener::class)
class Meeting(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

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
) {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime

    fun updateStatus(newStatus: MeetingStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw BusinessException(ErrorCode.INVALID_STATUS_TRANSITION)
        }
        this.status = newStatus
    }

    fun validateCreator(userId: UUID) {
        if (creator.id != userId) {
            throw BusinessException(ErrorCode.NOT_MEETING_CREATOR)
        }
    }
}