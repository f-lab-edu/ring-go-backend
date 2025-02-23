package com.ringgo.domain.expense.dto

import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
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

            @field:NotNull(message = "지출일자는 필수입니다")
            @Schema(description = "지출일자", example = "2025-02-14T12:00:00Z")
            val expenseDate: Instant,

            @field:NotNull(message = "무지출 여부는 필수입니다")
            @Schema(description = "무지출 여부", example = "false")
            val isNoExpense: Boolean,

            // 실제 지출인 경우에만 사용되는 필드들
            @Schema(description = "지출명", example = "점심식사")
            val name: String?,

            @Schema(description = "금액", example = "15000")
            val amount: BigDecimal?,

            @Schema(description = "카테고리", example = "FOOD")
            val category: ExpenseCategory?,

            @Schema(description = "설명", example = "어제 야근하느라 힘들어서...")
            val description: String?
        )

        @Schema(description = "지출 생성 응답", name = "ExpenseCreateResponse")
        data class Response(
            @Schema(description = "지출 ID")
            val id: Long,
            @Schema(description = "생성일시")
            val createdAt: Instant
        )
    }
}