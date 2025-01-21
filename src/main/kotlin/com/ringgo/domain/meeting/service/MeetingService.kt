package com.ringgo.domain.meeting.service

import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.member.entity.Member
import com.ringgo.domain.meeting.repository.MeetingRepository
import com.ringgo.domain.member.entity.enums.MemberRole
import com.ringgo.domain.user.entity.User
import com.ringgo.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

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
                    status = meeting.status.name,
                    memberCount = meeting.memberCount,
                    createdAt = meeting.createdAt.format(DateTimeFormatter.ISO_DATE_TIME),
                    isCreator = meeting.creator.id == user.id
                )
            }
    }
}