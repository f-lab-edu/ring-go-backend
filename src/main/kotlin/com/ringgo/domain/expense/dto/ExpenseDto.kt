package com.ringgo.domain.expense.dto

import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

class ExpenseDto {
    data class ExpenseItem(
        val id: Long,
        val name: String,
        val amount: BigDecimal,
        val category: ExpenseCategory,
        val description: String?,
        val createdAt: Instant
    )

    data class UserExpenseCommon(
        val userId: UUID,
        val userName: String,
        val expenses: List<ExpenseItem>,
        val totalAmount: BigDecimal
    )

    data class DailyExpense(
        @Schema(description = "날짜")
        val date: Instant,
        @Schema(description = "사용자별 지출 내역")
        val userExpenses: List<UserExpenseCommon>
    )

    @Schema(description = "지출 생성")
    class Create {
        @Schema(description = "지출 생성 요청", name = "ExpenseCreateRequest")
        data class Request(
            @field:NotNull(message = "활동 ID는 필수입니다")
            @Schema(description = "활동 ID", example = "1")
            val activityId: Long,

            @field:NotBlank(message = "지출명은 필수입니다")
            @field:Size(min = 1, max = 20, message = "지출명은 1자 이상 20자 이하여야 합니다")
            @Schema(description = "지출명", example = "점심식사")
            val name: String,

            @field:NotNull(message = "금액은 필수입니다")
            @field:Positive(message = "금액은 0보다 커야 합니다")
            @Schema(description = "금액", example = "15000")
            val amount: BigDecimal,

            @field:NotNull(message = "카테고리는 필수입니다")
            @Schema(description = "카테고리", example = "FOOD")
            val category: ExpenseCategory,

            @field:Size(max = 200, message = "설명은 200자를 넘을 수 없습니다")
            @Schema(description = "설명")
            val description: String?,

            @field:NotNull(message = "지출일자는 필수입니다")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @Schema(description = "지출일자", example = "2024-02-23")
            val expenseDate: LocalDate
        )

        data class Response(
            val id: Long,
            val createdAt: Instant
        )
    }

    @Schema(description = "지출 수정")
    class Update {
        @Schema(description = "지출 수정 요청", name = "ExpenseUpdateRequest")
        data class Request(
            @field:Size(min = 1, max = 20, message = "지출명은 1자 이상 20자 이하여야 합니다")
            @Schema(description = "지출명")
            val name: String?,

            @field:Positive(message = "금액은 0보다 커야 합니다")
            @Schema(description = "금액")
            val amount: BigDecimal?,

            @Schema(description = "카테고리")
            val category: ExpenseCategory?,

            @field:Size(max = 200, message = "설명은 200자를 넘을 수 없습니다")
            @Schema(description = "설명")
            val description: String?,

            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @Schema(description = "지출일자", example = "2024-02-23")
            val expenseDate: LocalDate?
        )

        data class Response(
            val id: Long,
            val updatedAt: Instant
        )
    }

    @Schema(description = "지출 목록 조회")
    class Get {
        @Schema(description = "지출 목록 조회 요청", name = "ExpenseGetRequest")
        data class Request(
            @field:NotNull(message = "활동 ID는 필수입니다")
            @Schema(description = "활동 ID", example = "1")
            val activityId: Long,

            @Schema(description = "과거순 정렬 여부", example = "false")
            val sortOrder: Boolean = false
        )

        data class Response(
            val dailyExpenses: List<DailyExpense>
        )
    }

    @Schema(description = "지출 검색")
    class Search {
        @Schema(description = "지출 검색 요청", name = "ExpenseSearchRequest")
        data class Request(
            @field:NotNull(message = "활동 ID는 필수입니다")
            @Schema(description = "활동 ID", example = "1")
            val activityId: Long,

            @field:Size(max = 100, message = "검색어는 100자를 넘을 수 없습니다")
            @Schema(description = "검색어", example = "식사")
            val keyword: String?,

            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @Schema(description = "시작일", example = "2024-02-01")
            val startDate: LocalDate?,

            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @Schema(description = "종료일", example = "2024-02-29")
            val endDate: LocalDate?,

            @Schema(description = "정렬 순서", example = "false")
            val sortOrder: Boolean = false,

            @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
            @Schema(description = "페이지 번호", example = "0")
            val page: Int = 0,

            @field:Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @field:Max(value = 100, message = "페이지 크기는 100을 초과할 수 없습니다")
            @Schema(description = "페이지 크기", example = "20")
            val size: Int = 20
        )

        data class Response(
            val dailyExpenses: List<DailyExpense>,
            val metadata: SearchMetadata
        )

        data class SearchMetadata(
            val totalElements: Long,
            val totalPages: Int,
            val currentPage: Int,
            val pageSize: Int,
            val totalAmount: BigDecimal
        )
    }
}