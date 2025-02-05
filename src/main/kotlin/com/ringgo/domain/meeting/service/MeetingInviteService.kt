package com.ringgo.domain.meeting.service

import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.config.MeetingInviteConfig
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.meeting.entity.MeetingInvite
import com.ringgo.domain.meeting.repository.MeetingInviteRepository
import com.ringgo.domain.meeting.repository.MeetingRepository
import com.ringgo.domain.member.repository.MemberRepository
import com.ringgo.domain.user.entity.User
import org.springframework.dao.DataIntegrityViolationException
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
    @Transactional
    fun createInviteLink(id: UUID, user: User): MeetingDto.InviteLink.CreateResponse {
        val meeting = meetingRepository.findByIdOrNull(id)
            ?: throw BusinessException(ErrorCode.MEETING_NOT_FOUND)

        val invite = generateUniqueInviteCode(meeting, user, memberRepository)

        return MeetingDto.InviteLink.CreateResponse(
            inviteUrl = "${meetingInviteConfig.baseUrl}/meeting/invite/${invite.code}",
            expiredAt = DateTimeFormatter.ISO_INSTANT.format(invite.expiredAt)
        )
    }

    private fun generateUniqueInviteCode(
        meeting: Meeting,
        user: User,
        memberRepository: MemberRepository
    ): MeetingInvite {
        var attempt = 0
        while (attempt <= meetingInviteConfig.maxRetries) {
            try {
                return meetingInviteRepository.save(
                    MeetingInvite.create(
                        meeting = meeting,
                        creator = user,
                        expirationDays = meetingInviteConfig.expirationDays,
                        memberRepository = memberRepository
                    )
                )
            } catch (e: DataIntegrityViolationException) {
                attempt++
            }
        }
        throw BusinessException(ErrorCode.CODE_GENERATION_FAILED)
    }
}