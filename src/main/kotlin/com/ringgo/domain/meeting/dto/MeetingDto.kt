package com.ringgo.domain.meeting.dto

import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
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
            @Schema(
                description = "모임 상태",
                example = "ACTIVE",
                implementation = MeetingStatus::class
            )
            val status: MeetingStatus,
            val memberCount: Long,
            val createdAt: Instant,
            val isCreator: Boolean
        )
    }

    @Schema(description = "모임 상태 변경")
    class UpdateStatus {
        @Schema(description = "모임 상태 변경 요청")
        data class Request(
            @Schema(
                description = "변경할 상태",
                example = "ENDED",
                implementation = MeetingStatus::class
            )
            val status: MeetingStatus
        )

        @Schema(description = "모임 상태 변경 응답")
        data class Response(
            @Schema(description = "모임 ID")
            val id: UUID,

            @Schema(
                description = "변경된 상태",
                example = "ENDED",
                implementation = MeetingStatus::class
            )
            val status: MeetingStatus
        )
    }

    @Schema(description = "모임 초대 링크")
    class InviteLink {
        @Schema(description = "초대 링크 생성 응답")
        data class CreateResponse(
            @Schema(description = "초대 링크 URL")
            val inviteUrl: String,

            @Schema(
                description = "만료일시 (ISO 8601 형식)",
                example = "2024-03-01T12:34:56Z"
            )
            val expiredAt: String
        )
    }
}