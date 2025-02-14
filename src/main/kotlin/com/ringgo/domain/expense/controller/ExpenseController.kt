package com.ringgo.domain.expense.controller

import com.ringgo.domain.expense.dto.ExpenseDto
import com.ringgo.domain.expense.service.ExpenseService
import com.ringgo.domain.user.entity.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Expense", description = "지출 API")
@RestController
@RequestMapping("/api/v1/expense")
class ExpenseController(
    private val expenseService: ExpenseService
) {
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
        return expenseService.create(request, user)
    }
}