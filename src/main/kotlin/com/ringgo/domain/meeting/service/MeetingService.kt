package com.ringgo.domain.meeting.service

import com.ringgo.common.exception.ApplicationException
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
                    createdAt = meeting.createdAt,
                    isCreator = meeting.creator.id == user.id
                )
            }
    }

    @Transactional
    fun updateStatus(meetingId: UUID, request: MeetingDto.UpdateStatus.Request, user: User): MeetingDto.UpdateStatus.Response {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
            ?: throw ApplicationException(ErrorCode.MEETING_NOT_FOUND)

        // 1. 멤버 여부 확인
        val member = memberRepository.findByMeetingIdAndUserId(meetingId, user.id)
            ?: throw ApplicationException(ErrorCode.NOT_MEETING_MEMBER)

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

    fun getMembers(meetingId: UUID, user: User): List<MeetingDto.Member.Response> {
        // 1. 사용자가 해당 모임의 멤버인지 확인
        if (!memberRepository.existsByMeetingIdAndUserId(meetingId, user.id)) {
            throw ApplicationException(ErrorCode.NOT_MEETING_MEMBER)
        }

        // 2. 모임원 목록 조회
        return memberRepository.findByMeetingIdOrderByJoinedAtAsc(meetingId)
            .map { member ->
                MeetingDto.Member.Response(
                    id = member.id,
                    userId = member.user.id,
                    name = member.user.name,
                    email = member.user.email,
                    role = member.role.name,
                    joinedAt = member.joinedAt,
                )
            }
    }

    @Transactional
    fun kickMember(meetingId: UUID, memberId: UUID, user: User) {
        // 1. 요청자의 멤버십 확인 및 권한 검증
        val requesterMember = memberRepository.findByMeetingIdAndUserId(meetingId, user.id)
            ?: throw ApplicationException(ErrorCode.NOT_MEETING_MEMBER)

        requesterMember.validateCreatorRole()

        // 2. 대상 모임원 조회
        val targetMember = memberRepository.findById(memberId)
            .orElseThrow { ApplicationException(ErrorCode.MEMBER_NOT_FOUND) }

        // 3. 자기 자신을 내보낼 수 없음
        if (targetMember.user.id == user.id) {
            throw ApplicationException(ErrorCode.CANNOT_KICK_SELF)
        }

        // 4. 대상 모임원이 해당 모임의 멤버가 맞는지 확인
        if (targetMember.meeting.id != meetingId) {
            throw ApplicationException(ErrorCode.MEMBER_NOT_FOUND)
        }

        // 5. 모임원 삭제
        memberRepository.delete(targetMember)
    }
}