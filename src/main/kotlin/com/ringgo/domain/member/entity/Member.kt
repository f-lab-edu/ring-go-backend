package com.ringgo.domain.member.entity

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.member.entity.enums.MemberRole
import com.ringgo.domain.member.entity.enums.MemberStatus
import com.ringgo.domain.user.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

private val log = KotlinLogging.logger {}

@Entity
@Table(
    name = "member",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_meeting_user",
            columnNames = ["meeting_id", "user_id"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Member(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: Meeting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: MemberRole,

    @Enumerated(EnumType.STRING)
    var status: MemberStatus = MemberStatus.ACTIVE,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: Instant = Instant.now(),
) {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant

    fun validateCreatorRole() {
        if (role != MemberRole.CREATOR) {
            log.warn { "User is not creator - memberId: $id, userId: ${user.id}, userRole: $role" }
            throw ApplicationException(ErrorCode.NOT_MEETING_CREATOR)
        }
    }

    fun updateStatus(newStatus: MemberStatus) {
        this.status = newStatus
    }
}