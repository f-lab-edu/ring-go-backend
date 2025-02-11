package com.ringgo.domain.activity.service

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.activity.dto.ActivityDto
import com.ringgo.domain.activity.entity.ExpenseActivity
import com.ringgo.domain.activity.entity.enums.ActivityType
import com.ringgo.domain.activity.repository.ActivityRepository
import com.ringgo.domain.meeting.repository.MeetingRepository
import com.ringgo.domain.member.repository.MemberRepository
import com.ringgo.domain.user.entity.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ActivityService(
    private val activityRepository: ActivityRepository,
    private val meetingRepository: MeetingRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional
    fun create(request: ActivityDto.Create.Request, user: User): ActivityDto.Create.Response {
        // 1. 모임 존재 여부 확인
        val meeting = meetingRepository.findByIdOrNull(request.meetingId)
            ?: throw ApplicationException(ErrorCode.MEETING_NOT_FOUND)

        // 2. 사용자가 모임의 멤버인지 확인
        val member = memberRepository.findByMeetingIdAndUserId(request.meetingId, user.id)
            ?: throw ApplicationException(ErrorCode.NOT_MEETING_MEMBER)

        // 3. 활동 타입 중복 체크
        if (activityRepository.existsByMeetingIdAndType(meeting.id, request.type)) {
            throw ApplicationException(ErrorCode.DUPLICATE_ACTIVITY_TYPE)
        }

        // 4. 활동 생성
        val activity = when(request.type) {
            ActivityType.EXPENSE -> ExpenseActivity.create(
                meeting = meeting,
                creator = user
            )
        }

        val savedActivity = activityRepository.save(activity)
        return ActivityDto.Create.Response(savedActivity.id)
    }
}