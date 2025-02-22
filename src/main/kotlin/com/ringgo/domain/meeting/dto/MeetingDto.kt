package com.ringgo.domain.meeting.dto

import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.member.entity.enums.MemberStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

class MeetingDto {

    @Schema(description = "모임 생성")
    class Create {
        @Schema(description = "모임 생성 요청", name = "MeetingCreateRequest")
        data class Request(
            @field:NotBlank(message = "모임 이름은 필수입니다")
            @field:Size(min = 2, max = 50, message = "모임 이름은 2~50자 사이여야 합니다")
            @Schema(description = "모임 이름", example = "우리 가좍 소비 모임")
            val name: String,

            @field:NotBlank(message = "모임 아이콘은 필수입니다")
            @Schema(description = "모임 아이콘", example = "family_icon.png")
            val icon: String
        )

        @Schema(description = "모임 생성 응답", name = "MeetingCreateResponse")
        data class Response(
            val id: UUID
        )
    }

    @Schema(description = "모임 조회")
    class Get {
        @Schema(description = "모임 조회 응답", name = "MeetingGetResponse")
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
        @Schema(description = "모임 상태 변경 요청", name = "MeetingStatusUpdateRequest")
        data class Request(
            @Schema(
                description = "변경할 상태",
                example = "ENDED",
                implementation = MeetingStatus::class
            )
            val status: MeetingStatus
        )

        @Schema(description = "모임 상태 변경 응답", name = "MeetingStatusUpdateResponse")
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

    @Schema(description = "모임 초대")
    class Invite {
        @Schema(description = "초대 링크 생성")
        class Create {
            @Schema(description = "초대 링크 생성 응답", name = "InviteLinkCreateResponse")
            data class Response(
                @Schema(description = "초대 링크 URL")
                val inviteUrl: String,

                @Schema(
                    description = "만료일시 (ISO 8601 형식)",
                    example = "2024-03-01T12:34:56Z"
                )
                val expiredAt: String
            )
        }

        @Schema(description = "초대 링크 참여")
        class Join {
            @Schema(description = "초대 링크 참여 응답", name = "InviteLinkJoinResponse")
            data class Response(
                @Schema(description = "모임 ID")
                val meetingId: UUID,

                @Schema(description = "모임 이름")
                val meetingName: String
            )
        }
    }

    @Schema(description = "모임원 관리")
    class Member {
        @Schema(description = "모임원 목록 조회")
        class Get {
            @Schema(description = "모임원 목록 조회 응답", name = "MeetingMemberListResponse")
            data class Response(
                @Schema(description = "모임원 ID")
                val id: UUID,

                @Schema(description = "사용자 ID")
                val userId: UUID,

                @Schema(description = "사용자 이름")
                val name: String,

                @Schema(description = "사용자 이메일")
                val email: String,

                @Schema(description = "모임원 역할")
                val role: String,

                @Schema(description = "가입일시")
                val joinedAt: Instant,
            )
        }

        @Schema(description = "모임원 내보내기")
        class Kick {
            @Schema(description = "모임원 내보내기 응답", name = "MeetingKickMemberResponse")
            data class Response(
                @Schema(description = "모임원 ID")
                val id: UUID,

                @Schema(
                    description = "변경된 상태",
                    example = "KICKED",
                    implementation = MemberStatus::class
                )
                val status: MemberStatus
            )
        }
    }
}