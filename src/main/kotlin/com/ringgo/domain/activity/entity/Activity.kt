package com.ringgo.domain.activity.entity

import com.ringgo.domain.activity.entity.enums.ActivityStatus
import com.ringgo.domain.activity.entity.enums.ActivityType
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "activity")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@EntityListeners(AuditingEntityListener::class)
abstract class Activity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: Meeting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(name = "type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    val type: ActivityType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ActivityStatus = ActivityStatus.ACTIVE
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0L

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant

    fun updateStatus(newStatus: ActivityStatus) {
        status.validateTransitionTo(newStatus)
        this.status = newStatus
    }
}