package com.ringgo.domain.meeting.controller

import com.ringgo.common.dto.CommonResponse
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.service.MeetingService
import com.ringgo.domain.user.entity.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus

@Tag(name = "Meeting", description = "모임 API")
@RestController
@RequestMapping("/api/v1/meeting")
class MeetingController(
    private val meetingService: MeetingService
) {
    @Operation(summary = "모임 생성", description = "새로운 모임을 생성합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "모임 생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    ])
    @PostMapping
    fun create(
        @Valid @RequestBody request: MeetingDto.Create.Request,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<CommonResponse<MeetingDto.Create.Response>> {
        val response = meetingService.create(request, user)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.created(response, "모임이 성공적으로 생성되었습니다."))
    }

    @Operation(summary = "모임 목록 조회", description = "사용자가 가입한 모든 모임 목록을 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "모임 목록 조회 성공")
    ])
    @GetMapping
    fun getMyMeeting(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<CommonResponse<List<MeetingDto.Get.Response>>> {
        val response = meetingService.getMyMeeting(user)
        return ResponseEntity.ok(CommonResponse.success(response))
    }
}
