package com.ringgo.domain.expense.controller

import com.ringgo.domain.expense.dto.ExpenseDto
import com.ringgo.domain.expense.service.ExpenseService
import com.ringgo.domain.user.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Expense", description = "지출 API")
@RestController
@RequestMapping("/api/v1/expense")
class ExpenseController(
    private val expenseService: ExpenseService
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Operation(summary = "지출 생성", description = "새로운 지출을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "지출 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: ExpenseDto.Create.Request,
        @AuthenticationPrincipal user: User
    ): ExpenseDto.Create.Response {
        log.info { "Create expense request: $request" }
        return expenseService.create(request, user)
    }

    @Operation(summary = "지출 수정", description = "지출 기록을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "지출 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "지출 기록을 찾을 수 없음")
        ]
    )
    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ExpenseDto.Update.Request,
        @AuthenticationPrincipal user: User
    ): ExpenseDto.Update.Response {
        log.info { "Update expense request: $request" }
        return expenseService.update(id, request, user)
    }

    @Operation(summary = "지출 삭제", description = "지출 기록을 삭제 처리합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "지출 삭제 성공"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "지출 기록을 찾을 수 없음")
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ) {
        log.info { "Delete expense request: $id" }
        expenseService.delete(id, user)
    }

    @Operation(summary = "지출 목록 조회", description = "지출 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
        ]
    )
    @GetMapping
    fun list(
        @Valid request: ExpenseDto.Get.Request,
        @AuthenticationPrincipal user: User
    ): ExpenseDto.Get.Response {
        log.info { "List expense request: $request" }
        return expenseService.list(request, user)
    }

    @Operation(summary = "지출 검색", description = "지출을 검색합니다. 검색어는 지출명, 사용자명, 금액, 설명을 통합 검색합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "검색 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
        ]
    )
    @GetMapping("/search")
    fun search(
        @RequestParam(name = "activityId") activityId: Long,
        @RequestParam(name = "keyword", required = false) keyword: String?,
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd") startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd") endDate: LocalDate?,
        @RequestParam(name = "sortOrder", required = false, defaultValue = "false") sortOrder: Boolean,
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(name = "size", required = false, defaultValue = "20") size: Int,
        @AuthenticationPrincipal user: User
    ): ExpenseDto.Search.Response {
        log.info { "Search expense request: activityId=$activityId, keyword=$keyword, startDate=$startDate, endDate=$endDate" }

        val request = ExpenseDto.Search.Request(
            activityId = activityId,
            keyword = keyword,
            startDate = startDate,
            endDate = endDate,
            sortOrder = sortOrder,
            page = page,
            size = size
        )
        request.validate()

        return expenseService.search(request, user)
    }
}