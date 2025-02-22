package com.ringgo.domain.expense.dto

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.Instant
import java.util.*

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
            @Schema(description = "설명", example = "어제 야근하느라 힘들어서 진짜 나한테 보상을 주고 싶었음.. 그래서 점심에 초밥 사먹었어요. ㅋㅋ")
            val description: String?,

            @field:NotNull(message = "지출일자는 필수입니다")
            @Schema(description = "지출일자", example = "2025-02-14T12:00:00Z")
            val expenseDate: Instant
        )

        @Schema(description = "지출 생성 응답", name = "ExpenseCreateResponse")
        data class Response(
            @Schema(description = "지출 ID")
            val id: Long,
            @Schema(description = "생성일시")
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

            @Schema(description = "지출일자")
            val expenseDate: Instant?
        )

        @Schema(description = "지출 수정 응답", name = "ExpenseUpdateResponse")
        data class Response(
            @Schema(description = "지출 ID")
            val id: Long,
            @Schema(description = "수정일시")
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

            @Schema(description = "과거순 정렬 여부 (true: 과거순, false: 최신순)", example = "false")
            val sortOrder: Boolean = false
        )

        @Schema(description = "지출 목록 조회 응답", name = "ExpenseGetResponse")
        data class Response(
            @Schema(description = "날짜별 지출 목록")
            val dailyExpenses: List<DailyExpense>
        )

        @Schema(description = "날짜별 지출 내역", name = "ExpenseGetDailyExpense")
        data class DailyExpense(
            @Schema(description = "날짜", example = "2024-02-15")
            val date: Instant,

            @Schema(description = "사용자별 지출 내역")
            val userExpenses: List<UserExpense>
        )

        @Schema(description = "사용자별 지출 내역", name = "ExpenseGetUserExpense")
        data class UserExpense(
            @Schema(description = "사용자 ID")
            val userId: UUID,

            @Schema(description = "사용자 이름")
            val userName: String,

            @Schema(description = "지출 목록")
            val expenses: List<ExpenseItem>,

            @Schema(description = "총 지출액")
            val totalAmount: BigDecimal
        )

        @Schema(description = "지출 항목", name = "ExpenseGetExpenseItem")
        data class ExpenseItem(
            val id: Long,
            val name: String,
            val amount: BigDecimal,
            val category: ExpenseCategory,
            val description: String?,
            val createdAt: Instant,
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
            @Schema(description = "검색어 (지출명, 사용자명, 금액, 설명)", example = "식사")
            val keyword: String?,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Schema(description = "시작일", example = "2024-02-01")
            val startDate: LocalDate?,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Schema(description = "종료일", example = "2024-02-29")
            val endDate: LocalDate?,

            @Schema(description = "정렬 순서 (true: 과거순, false: 최신순)", example = "false")
            val sortOrder: Boolean = false,

            @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
            @Schema(description = "페이지 번호", example = "0", defaultValue = "0")
            val page: Int = 0,

            @field:Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @field:Max(value = 100, message = "페이지 크기는 100을 초과할 수 없습니다")
            @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
            val size: Int = 20
        ) {
            fun validate() {
                validateDateRange()
                validateFutureDate()
            }

            private fun validateDateRange() {
                if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                    throw ApplicationException(ErrorCode.INVALID_DATE_RANGE)
                }
            }

            private fun validateFutureDate() {
                val now = LocalDate.now()
                if (startDate?.isAfter(now) == true || endDate?.isAfter(now) == true) {
                    throw ApplicationException(ErrorCode.INVALID_FUTURE_DATE)
                }
            }
        }

        @Schema(description = "지출 검색 응답", name = "ExpenseSearchResponse")
        data class Response(
            @Schema(description = "날짜별 지출 목록")
            val dailyExpenses: List<DailyExpense>,

            @Schema(description = "검색 결과 메타데이터")
            val metadata: SearchMetadata
        )

        @Schema(description = "검색 결과 메타데이터", name = "ExpenseSearchMetadata")
        data class SearchMetadata(
            @Schema(description = "총 레코드 수")
            val totalElements: Long,

            @Schema(description = "총 페이지 수")
            val totalPages: Int,

            @Schema(description = "현재 페이지 번호")
            val currentPage: Int,

            @Schema(description = "페이지 크기")
            val pageSize: Int,

            @Schema(description = "검색된 총 금액")
            val totalAmount: BigDecimal
        )

        @Schema(description = "날짜별 지출 내역", name = "ExpenseSearchDailyExpense")
        data class DailyExpense(
            @Schema(description = "날짜", example = "2024-02-15")
            val date: LocalDate,

            @Schema(description = "사용자별 지출 내역")
            val userExpenses: List<UserExpense>
        )

        @Schema(description = "사용자별 지출 내역", name = "ExpenseSearchUserExpense")
        data class UserExpense(
            @Schema(description = "사용자 ID")
            val userId: UUID,

            @Schema(description = "사용자 이름")
            val userName: String,

            @Schema(description = "지출 목록")
            val expenses: List<ExpenseItem>,

            @Schema(description = "총 지출액")
            val totalAmount: BigDecimal
        )

        @Schema(description = "지출 항목", name = "ExpenseSearchExpenseItem")
        data class ExpenseItem(
            val id: Long,
            val name: String,
            val amount: BigDecimal,
            val category: ExpenseCategory,
            val description: String?,
            val createdAt: Instant
        )
    }
}