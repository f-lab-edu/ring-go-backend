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

        // 4. 금액 검증
        if (request.amount <= BigDecimal.ZERO) {
            throw ApplicationException(ErrorCode.INVALID_EXPENSE_AMOUNT)
        }

        // 5. 날짜 검증
        if (request.expenseDate.isAfter(Instant.now())) {
            throw ApplicationException(ErrorCode.INVALID_INPUT_VALUE)
        }

        // 6. 지출 생성 및 저장
        val expense = expenseRepository.save(
            Expense(
                activity = activity,
                creator = user,
                name = request.name,
                amount = request.amount,
                category = request.category,
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

    @Transactional
    fun update(id: Long, request: ExpenseDto.Update.Request, user: User): ExpenseDto.Update.Response {
        // 1. 지출 조회
        val expense = expenseRepository.findByIdOrNull(id)
            ?: throw ApplicationException(ErrorCode.EXPENSE_NOT_FOUND)

        // 2. 요청 데이터 검증
        request.amount?.let {
            if (it <= BigDecimal.ZERO) {
                throw ApplicationException(ErrorCode.INVALID_EXPENSE_AMOUNT)
            }
        }

        request.expenseDate?.let {
            if (it.isAfter(Instant.now())) {
                throw ApplicationException(ErrorCode.INVALID_INPUT_VALUE)
            }
        }

        // 3. 지출 수정
        expense.update(request, user.id)
        log.info { "Expense updated: $id" }

        return ExpenseDto.Update.Response(
            id = expense.id,
            updatedAt = expense.updatedAt
        )
    }
}