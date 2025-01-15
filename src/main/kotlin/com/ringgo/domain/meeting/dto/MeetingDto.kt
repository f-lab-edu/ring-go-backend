package com.ringgo.domain.meeting.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

class MeetingDto {

    @Schema(description = "모임 생성")
    class Create {
        @Schema(description = "모임 생성 요청")
        data class Request(
            @field:NotBlank(message = "모임 이름은 필수입니다")
            @field:Size(min = 2, max = 50, message = "모임 이름은 2~50자 사이여야 합니다")
            @Schema(description = "모임 이름", example = "우리 가족 소비 모임")
            val name: String,

            @field:NotBlank(message = "모임 아이콘은 필수입니다")
            @Schema(description = "모임 아이콘", example = "family_icon.png")
            val icon: String
        )

        @Schema(description = "모임 생성 응답")
        data class Response(
            val id: UUID
        )
    }

    @Schema(description = "모임 조회")
    class Get {
        @Schema(description = "모임 조회 응답")
        data class Response(
            val id: UUID,
            val name: String,
            val icon: String,
            val status: String,
            val memberCount: Int,
            val createdAt: String,
            val isCreator: Boolean
        )
    }
}