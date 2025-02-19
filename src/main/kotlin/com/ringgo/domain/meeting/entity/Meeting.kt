package com.ringgo.domain.meeting.entity

import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
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
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant

    fun updateStatus(newStatus: MeetingStatus) {
        status.validateTransitionTo(newStatus)
        this.status = newStatus
    }
}