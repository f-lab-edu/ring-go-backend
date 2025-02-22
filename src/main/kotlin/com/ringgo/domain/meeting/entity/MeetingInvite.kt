package com.ringgo.domain.meeting.entity

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.member.repository.MemberRepository
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(name = "meeting_invite")
@EntityListeners(AuditingEntityListener::class)
class MeetingInvite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: Meeting,

    @Embedded
    val code: Code,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(nullable = false)
    val expiredAt: Instant,
) {
    fun validateValidity() {
        when {
            isExpired() -> throw ApplicationException(ErrorCode.EXPIRED_INVITE_LINK)
            !isMeetingActive() -> throw ApplicationException(ErrorCode.INACTIVE_MEETING)
        }
    }

    private fun isExpired(): Boolean =
        Instant.now().isAfter(expiredAt)

    private fun isMeetingActive(): Boolean =
        meeting.status == MeetingStatus.ACTIVE

    companion object {
        fun create(
            meeting: Meeting,
            creator: User,
            expirationDays: Long
        ): MeetingInvite {
            return MeetingInvite(
                meeting = meeting,
                creator = creator,
                code = Code.generate(),
                expiredAt = Instant.now().plus(expirationDays, ChronoUnit.DAYS)
            )
        }
    }

    @CreatedDate
    lateinit var createdAt: Instant

    @LastModifiedDate
    lateinit var updatedAt: Instant
}