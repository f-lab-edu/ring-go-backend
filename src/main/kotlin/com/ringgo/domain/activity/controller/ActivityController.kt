package com.ringgo.domain.activity.controller

import com.ringgo.domain.activity.dto.ActivityDto
import com.ringgo.domain.activity.entity.enums.ActivityType
import com.ringgo.domain.activity.service.ActivityService
import com.ringgo.domain.user.entity.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Activity", description = "활동 API")
@RestController
@RequestMapping("/api/v1/activity")
class ActivityController(
    private val activityService: ActivityService
) {
    @Operation(summary = "활동 생성", description = "새로운 활동을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "활동 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "권한 없음")
        ]
    )
    @PostMapping("/{meetingId}/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable meetingId: UUID,
        @PathVariable type: ActivityType,
        @AuthenticationPrincipal user: User
    ): ActivityDto.Create.Response {
        return activityService.create(ActivityDto.Create.Request(meetingId, type), user)
    }
}