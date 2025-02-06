package com.ringgo.domain.meeting.service

import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.member.entity.Member
import com.ringgo.domain.meeting.repository.MeetingRepository
import com.ringgo.domain.member.entity.enums.MemberRole
import com.ringgo.domain.user.entity.User
import com.ringgo.domain.member.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
@Transactional(readOnly = true)
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional
    fun create(request: MeetingDto.Create.Request, user: User): MeetingDto.Create.Response {
        val meeting = Meeting(
            name = request.name,
            icon = request.icon,
            creator = user,
            status = MeetingStatus.ACTIVE
        )

        val savedMeeting = meetingRepository.save(meeting)

        // 생성자를 멤버로 추가
        memberRepository.save(
            Member(
                meeting = savedMeeting,
                user = user,
                role = MemberRole.CREATOR
            )
        )

        return MeetingDto.Create.Response(savedMeeting.id)
    }

    fun getMyMeeting(user: User): List<MeetingDto.Get.Response> {
        return memberRepository.findMeetingWithMemberCount(user.id)
            .map { meeting ->
                MeetingDto.Get.Response(
                    id = meeting.id,
                    name = meeting.name,
                    icon = meeting.icon,
                    status = meeting.status,
                    memberCount = meeting.memberCount,
                    createdAt = DateTimeFormatter.ISO_INSTANT.format(meeting.createdAt),
                    isCreator = meeting.creator.id == user.id
                )
            }
    }

    @Transactional
    fun updateStatus(id: UUID, request: MeetingDto.UpdateStatus.Request, user: User): MeetingDto.UpdateStatus.Response {
        val meeting = meetingRepository.findByIdOrNull(id)
            ?: throw BusinessException(ErrorCode.MEETING_NOT_FOUND)

        // 1. 멤버 여부 확인
        val member = memberRepository.findByMeetingIdAndUserId(id, user.id)
            ?: throw BusinessException(ErrorCode.NOT_MEETING_MEMBER)

        // 2. CREATOR 역할 확인
        member.validateCreatorRole()

        // 3. 상태 변경 시도
        meeting.updateStatus(request.status)

        // 4. Response 반환
        return MeetingDto.UpdateStatus.Response(
            id = meeting.id,
            status = meeting.status
        )
    }
}