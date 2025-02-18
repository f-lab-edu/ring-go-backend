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
@Table(name = "invite_links")
@EntityListeners(AuditingEntityListener::class)
class MeetingInvite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: Meeting,

    @Column(nullable = false, length = 15, unique = true)
    val code: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(nullable = false)
    val expiredAt: Instant,
) {
    companion object {
        private const val CODE_LENGTH = 15

        fun create(
            meeting: Meeting,
            creator: User,
            expirationDays: Long,
            memberRepository: MemberRepository
        ): MeetingInvite {
            validateMemberLimit(meeting, memberRepository)

            return MeetingInvite(
                meeting = meeting,
                creator = creator,
                code = generateUniqueCode(generateBaseCode()),
                expiredAt = Instant.now().plus(expirationDays, ChronoUnit.DAYS)
            )
        }

        private fun validateMemberLimit(meeting: Meeting, memberRepository: MemberRepository) {
            val memberCount = memberRepository.countByMeetingId(meeting.id)
            if (memberCount >= 5) {
                throw ApplicationException(ErrorCode.MEETING_MEMBER_LIMIT_EXCEEDED)
            }
        }

        private fun generateBaseCode() =
            (UUID.randomUUID().toString() + System.nanoTime())
                .replace("-", "")
                .take(CODE_LENGTH)

        private fun generateUniqueCode(base: String, attempt: Int = 0): String {
            val suffix = if (attempt > 0) attempt.toString() else ""
            return base.take(CODE_LENGTH - suffix.length) + suffix
        }
    }

    @CreatedDate
    lateinit var createdAt: Instant

    @LastModifiedDate
    lateinit var updatedAt: Instant
}