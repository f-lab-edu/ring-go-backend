package com.ringgo.domain.meeting.controller

import com.ringgo.domain.meeting.dto.MeetingDto
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
    private val meetingService: MeetingService
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
    ) {
        meetingService.updateStatus(id, request, user)
    }

}