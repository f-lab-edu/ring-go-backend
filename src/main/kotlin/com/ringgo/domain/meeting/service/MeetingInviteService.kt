package com.ringgo.domain.meeting.service

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.config.MeetingInviteConfig
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.meeting.entity.MeetingInvite
import com.ringgo.domain.meeting.repository.MeetingInviteRepository
import com.ringgo.domain.meeting.repository.MeetingRepository
import com.ringgo.domain.member.entity.Member
import com.ringgo.domain.member.entity.enums.MemberRole
import com.ringgo.domain.member.repository.MemberRepository
import com.ringgo.domain.user.entity.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class MeetingInviteService(
    private val meetingRepository: MeetingRepository,
    private val meetingInviteRepository: MeetingInviteRepository,
    private val memberRepository: MemberRepository,
    private val meetingInviteConfig: MeetingInviteConfig
) {
    fun createInviteLink(id: UUID, user: User): MeetingDto.InviteLink.CreateResponse {
        val meeting = meetingRepository.findByIdOrNull(id)
            ?: throw ApplicationException(ErrorCode.MEETING_NOT_FOUND)

        validateMemberLimit(meeting)

        val invite = meetingInviteRepository.save(
            MeetingInvite.create(
                meeting = meeting,
                creator = user,
                expirationDays = meetingInviteConfig.inviteExpirationDays
            )
        )

        return MeetingDto.Invite.Create.Response(
            inviteUrl = "${meetingInviteConfig.baseUrl}/meeting/invite/${invite.code}",
            expiredAt = DateTimeFormatter.ISO_INSTANT.format(invite.expiredAt)
        )
    }

    @Transactional
    fun joinWithInviteCode(code: String, user: User): Member {
        val invite = meetingInviteRepository.findByCode(code)
            ?: throw ApplicationException(ErrorCode.INVALID_INVITE_LINK)

        invite.validateValidity()
        validateExistingMember(invite.meeting, user)
        validateMemberLimit(invite.meeting)

        return memberRepository.save(
            Member(
                meeting = invite.meeting,
                user = user,
                role = MemberRole.MEMBER
            )
        )
    }

    private fun validateExistingMember(meeting: Meeting, user: User) {
        if (memberRepository.existsByMeetingIdAndUserId(meeting.id, user.id)) {
            throw ApplicationException(ErrorCode.ALREADY_JOINED_MEMBER)
        }
    }

    private fun validateMemberLimit(meeting: Meeting) {
        val memberCount = memberRepository.countByMeetingId(meeting.id)
        if (memberCount >= meetingInviteConfig.maxMembers) {
            throw ApplicationException(ErrorCode.MEETING_MEMBER_LIMIT_EXCEEDED)
        }
    }
}