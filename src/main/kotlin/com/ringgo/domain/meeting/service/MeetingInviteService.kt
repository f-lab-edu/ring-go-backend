package com.ringgo.domain.meeting.service

import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.config.MeetingInviteConfig
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.meeting.entity.MeetingInvite
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.meeting.repository.MeetingInviteRepository
import com.ringgo.domain.meeting.repository.MeetingRepository
import com.ringgo.domain.member.entity.Member
import com.ringgo.domain.member.entity.enums.MemberRole
import com.ringgo.domain.member.repository.MemberRepository
import com.ringgo.domain.user.entity.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class MeetingInviteService(
    private val meetingRepository: MeetingRepository,
    private val meetingInviteRepository: MeetingInviteRepository,
    private val memberRepository: MemberRepository,
    private val meetingInviteConfig: MeetingInviteConfig
) {
    @Transactional
    fun createInviteLink(id: UUID, user: User): MeetingDto.InviteLink.CreateResponse {
        val meeting = meetingRepository.findByIdOrNull(id)
            ?: throw BusinessException(ErrorCode.MEETING_NOT_FOUND)

        val invite = meetingInviteRepository.save(
            MeetingInvite.create(
                meeting = meeting,
                creator = user,
                expirationDays = meetingInviteConfig.inviteExpirationDays,
                memberRepository = memberRepository
            )
        )

        return MeetingDto.InviteLink.CreateResponse(
            inviteUrl = "${meetingInviteConfig.baseUrl}/meeting/invite/${invite.code}",
            expiredAt = DateTimeFormatter.ISO_INSTANT.format(invite.expiredAt)
        )
    }

    @Transactional
    fun joinWithInviteCode(code: String, user: User): Member {
        val invite = meetingInviteRepository.findByCode(code)
            ?: throw BusinessException(ErrorCode.INVALID_INVITE_LINK)

        validateInviteValidity(invite)
        checkExistingMembership(invite.meeting, user)
        checkMemberLimit(invite.meeting)

        return memberRepository.save(
            Member(
                meeting = invite.meeting,
                user = user,
                role = MemberRole.MEMBER
            )
        )
    }

    private fun validateInviteValidity(invite: MeetingInvite) {
        when {
            Instant.now().isAfter(invite.expiredAt) ->
                throw BusinessException(ErrorCode.EXPIRED_INVITE_LINK)

            invite.meeting.status != MeetingStatus.ACTIVE ->
                throw BusinessException(ErrorCode.INACTIVE_MEETING)
        }
    }

    private fun checkExistingMembership(meeting: Meeting, user: User) {
        if (memberRepository.existsByMeetingIdAndUserId(meeting.id, user.id)) {
            throw BusinessException(ErrorCode.ALREADY_JOINED_MEMBER)
        }
    }

    private fun checkMemberLimit(meeting: Meeting) {
        val memberCount = memberRepository.countByMeetingId(meeting.id)
        if (memberCount >= meetingInviteConfig.maxMembers) {
            throw BusinessException(ErrorCode.MEETING_MEMBER_LIMIT_EXCEEDED)
        }
    }
}