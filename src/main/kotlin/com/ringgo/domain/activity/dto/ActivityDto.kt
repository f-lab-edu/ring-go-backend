package com.ringgo.domain.activity.dto

import com.ringgo.domain.activity.entity.enums.ActivityType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.*

class ActivityDto {
    @Schema(description = "활동 생성")
    class Create {
        @Schema(description = "활동 생성 요청")
        data class Request(
            @field:NotNull(message = "모임 ID는 필수입니다")
            @Schema(description = "모임 ID")
            val meetingId: UUID,

            @field:NotNull(message = "활동 유형은 필수입니다")
            @Schema(description = "활동 유형", example = "EXPENDITURE")
            val type: ActivityType,
        )

        @Schema(description = "활동 생성 응답", name = "ActivityCreateResponse")
        data class Response(
            @Schema(description = "활동 ID")
            val id: Long
        )
    }
}
