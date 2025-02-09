package com.ringgo.domain.meeting.controller

import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.service.MeetingInviteService
import com.ringgo.domain.meeting.service.MeetingService
import com.ringgo.domain.user.entity.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import java.util.*

@Tag(name = "Meeting", description = "모임 API")
@RestController
@RequestMapping("/api/v1/meeting")
class MeetingController(
    private val meetingService: MeetingService,
    private val meetingInviteService: MeetingInviteService,
) {
    @Operation(summary = "모임 생성", description = "새로운 모임을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "모임 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: MeetingDto.Create.Request,
        @AuthenticationPrincipal user: User
    ): MeetingDto.Create.Response {
        return meetingService.create(request, user)
    }

    @Operation(summary = "모임 목록 조회", description = "사용자가 가입한 모든 모임 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "모임 목록 조회 성공")
        ]
    )
    @GetMapping
    fun getMyMeeting(@AuthenticationPrincipal user: User) =
        meetingService.getMyMeeting(user)

    @Operation(summary = "모임 상태 변경", description = "모임의 상태를 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "권한 없음")
        ]
    )
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MeetingDto.UpdateStatus.Request,
        @AuthenticationPrincipal user: User
    ): MeetingDto.UpdateStatus.Response {
        return meetingService.updateStatus(id, request, user)
    }

    @Operation(summary = "모임 초대 링크 생성", description = "모임의 초대 링크를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "초대 링크 생성 성공"),
            ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
        ]
    )
    @PostMapping("/{id}/invite")
    @ResponseStatus(HttpStatus.CREATED)
    fun createInviteLink(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: User
    ): MeetingDto.InviteLink.CreateResponse = meetingInviteService.createInviteLink(id, user)

    @Operation(summary = "초대 코드로 모임 참여", description = "초대 코드를 사용하여 모임에 참여합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "모임 참여 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "유효하지 않은 초대 코드"),
            ApiResponse(responseCode = "409", description = "이미 가입된 멤버")
        ]
    )
    @PostMapping("/invite/{code}")
    fun joinMeeting(
        @PathVariable code: String,
        @AuthenticationPrincipal user: User
    ): MeetingDto.InviteLink.JoinResponse {
        val member = meetingInviteService.joinWithInviteCode(code, user)
        return MeetingDto.InviteLink.JoinResponse(
            meetingId = member.meeting.id,
            meetingName = member.meeting.name
        )
    }

    @Operation(summary = "모임원 목록 조회", description = "특정 모임의 모든 모임원 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "모임원 목록 조회 성공"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
        ]
    )
    @GetMapping("/{id}/members")
    fun getMembers(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: User
    ): List<MeetingDto.Member.Response> {
        return meetingService.getMembers(id, user)
    }

    @Operation(summary = "모임원 내보내기", description = "특정 모임원을 모임에서 내보냅니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "모임원 내보내기 성공",
            ),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "모임 또는 모임원을 찾을 수 없음")
        ]
    )
    @DeleteMapping("/{meetingId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun kickMember(
        @PathVariable meetingId: UUID,
        @PathVariable memberId: UUID,
        @AuthenticationPrincipal user: User
    ) {
        meetingService.kickMember(meetingId, memberId, user)
    }
}