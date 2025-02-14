package com.ringgo.domain.expense.entity

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.activity.entity.ExpenseActivity
import com.ringgo.domain.expense.dto.ExpenseDto
import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
class Expense(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    val activity: ExpenseActivity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(nullable = false, length = 100)
    private var name: String,

    @Column(nullable = false, precision = 10, scale = 2)
    private var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private var category: ExpenseCategory,

    @Column(columnDefinition = "TEXT")
    private var description: String?,

    @Column(nullable = false)
    private var expenseDate: Instant
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant

    init {
        validateAmount(amount)
        validateExpenseDate(expenseDate)
    }

    fun update(request: ExpenseDto.Update.Request, requesterId: UUID) {
        validateCreator(requesterId)

        // null이 아닌 필드만 업데이트
        request.name?.let { this.name = it }
        request.amount?.let {
            validateAmount(it)
            this.amount = it
        }
        request.category?.let { this.category = it }
        request.description?.let { this.description = it }
        request.expenseDate?.let {
            validateExpenseDate(it)
            this.expenseDate = it
        }
    }

    private fun validateAmount(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            throw ApplicationException(ErrorCode.INVALID_EXPENSE_AMOUNT)
        }
    }

    private fun validateExpenseDate(expenseDate: Instant) {
        if (expenseDate.isAfter(Instant.now())) {
            throw ApplicationException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }

    private fun validateCreator(requesterId: UUID) {
        if (creator.id != requesterId) {
            throw ApplicationException(ErrorCode.NOT_EXPENSE_CREATOR)
        }
    }
}