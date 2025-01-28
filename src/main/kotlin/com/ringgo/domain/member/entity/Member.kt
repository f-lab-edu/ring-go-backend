package com.ringgo.domain.member.entity

import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.member.entity.enums.MemberRole
import com.ringgo.domain.user.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

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

    @Column(nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime

    fun validateCreatorRole() {
        if (role != MemberRole.CREATOR) {
            log.warn { "User is not creator - memberId: $id, userId: ${user.id}, userRole: $role" }
            throw BusinessException(ErrorCode.NOT_MEETING_CREATOR)
        }
    }
}