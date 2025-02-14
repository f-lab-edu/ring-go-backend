package com.ringgo.domain.expense.dto

import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

class ExpenseDto {
    @Schema(description = "지출 생성")
    class Create {
        @Schema(description = "지출 생성 요청", name = "ExpenseCreateRequest")
        data class Request(
            @field:NotNull(message = "활동 ID는 필수입니다")
            @Schema(description = "활동 ID", example = "1")
            val activityId: Long,

            @field:NotBlank(message = "지출명은 필수입니다")
            @field:Size(min = 1, max = 20, message = "지출명은 1자 이상 20자 이하여야 합니다")
            @Schema(description = "지출명", example = "갓덴스시")
            val name: String,

            @field:NotNull(message = "금액은 필수입니다")
            @field:Positive(message = "금액은 0보다 커야 합니다")
            @Schema(
                description = "금액",
                example = "56000.00",
                type = "number",
                format = "decimal",
                minimum = "0.01",
                maximum = "9999999999.99"
            )
            val amount: BigDecimal,

            @field:NotNull(message = "카테고리는 필수입니다")
            @Schema(
                description = "카테고리 (FOOD/SHOPPING/TRANSPORT/LIVING/MEDICAL/OTHER)",
                example = "FOOD",
                implementation = ExpenseCategory::class
            )
            val category: ExpenseCategory,

            @field:Size(max = 200, message = "설명은 200자를 넘을 수 없습니다")
            @Schema(
                description = "설명 (최대 200자)",
                example = "어제 야근하느라 힘들어서 진짜 나한테 보상을 주고 싶었음.. 그래서 점심에 초밥 사먹었어요. ㅋㅋ",
                required = false
            )
            val description: String?,

            @field:NotNull(message = "지출일자는 필수입니다")
            @Schema(
                description = "지출일자",
                example = "2025-02-14T12:00:00Z",
                type = "string",
                format = "date-time"
            )
            val expenseDate: Instant
        )

        @Schema(description = "지출 생성 응답", name = "ExpenseCreateResponse")
        data class Response(
            @Schema(description = "지출 ID", example = "1")
            val id: Long,

            @Schema(
                description = "생성일시",
                example = "2025-02-14T12:00:00Z"
            )
            val createdAt: Instant
        )
    }
}