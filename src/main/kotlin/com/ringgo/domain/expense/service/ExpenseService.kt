package com.ringgo.domain.expense.service

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.activity.entity.ExpenseActivity
import com.ringgo.domain.activity.entity.enums.ActivityStatus
import com.ringgo.domain.activity.repository.ActivityRepository
import com.ringgo.domain.expense.dto.ExpenseDto
import com.ringgo.domain.expense.entity.Expense
import com.ringgo.domain.expense.repository.ExpenseRepository
import com.ringgo.domain.member.repository.MemberRepository
import com.ringgo.domain.user.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val activityRepository: ActivityRepository,
    private val memberRepository: MemberRepository
) {
    fun create(request: ExpenseDto.Create.Request, user: User): ExpenseDto.Create.Response {
        // 1. 활동 조회 및 검증
        val activity = activityRepository.findByIdOrNull(request.activityId)?.let { it as? ExpenseActivity }
            ?: throw ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND)

        // 2. 모임 멤버 검증
        memberRepository.findByMeetingIdAndUserId(activity.meeting.id, user.id)
            ?: throw ApplicationException(ErrorCode.NOT_MEETING_MEMBER)

        // 3. 활동 상태 검증
        if (activity.status != ActivityStatus.ACTIVE) {
            throw ApplicationException(ErrorCode.INACTIVE_ACTIVITY)
        }

        // 4. 날짜 검증
        if (request.expenseDate.isAfter(Instant.now())) {
            throw ApplicationException(ErrorCode.INVALID_INPUT_VALUE)
        }

        // 5. 데이터 검증
        if (request.isNoExpense) {
            validateNoExpenseData(request)
        } else {
            validateExpenseData(request)
        }

        // 6. 지출 생성 및 저장
        val expense = expenseRepository.save(
            Expense(
                activity = activity,
                creator = user,
                isNoExpense = request.isNoExpense,
                name = if (!request.isNoExpense) request.name else null,
                amount = if (!request.isNoExpense) request.amount else null,
                category = if (!request.isNoExpense) request.category else null,
                description = request.description,
                expenseDate = request.expenseDate
            )
        )

        log.info { "Expense created: ${expense.id}" }
        return ExpenseDto.Create.Response(
            id = expense.id,
            createdAt = expense.createdAt
        )
    }

    private fun validateExpenseData(request: ExpenseDto.Create.Request) {
        if (request.name.isNullOrBlank()) {
            throw ApplicationException(ErrorCode.EXPENSE_NAME_REQUIRED)
        }
        if (request.amount == null || request.amount <= BigDecimal.ZERO) {
            throw ApplicationException(ErrorCode.INVALID_EXPENSE_AMOUNT)
        }
        if (request.category == null) {
            throw ApplicationException(ErrorCode.EXPENSE_CATEGORY_REQUIRED)
        }
    }

    private fun validateNoExpenseData(request: ExpenseDto.Create.Request) {
        if (request.name != null || request.amount != null || request.category != null) {
            throw ApplicationException(ErrorCode.INVALID_NO_EXPENSE_DATA)
        }
    }
}