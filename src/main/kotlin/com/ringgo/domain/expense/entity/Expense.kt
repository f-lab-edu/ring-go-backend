package com.ringgo.domain.expense.entity

import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.activity.entity.ExpenseActivity
import com.ringgo.domain.activity.entity.enums.ActivityStatus
import com.ringgo.domain.expense.dto.ExpenseDto
import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import com.ringgo.domain.user.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "expense")
@SQLRestriction("is_deleted = false")
@EntityListeners(AuditingEntityListener::class)
class Expense(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    val activity: ExpenseActivity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var category: ExpenseCategory,

    @Column(columnDefinition = "TEXT")
    var description: String?,

    @Column(nullable = false)
    var expenseDate: LocalDate
) {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant

    @Column(nullable = false)
    var isDeleted: Boolean = false

    @Column
    var deletedAt: Instant? = null

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun update(request: ExpenseDto.Update.Request, requesterId: UUID) {
        if (creator.id != requesterId) {
            throw ApplicationException(ErrorCode.NOT_EXPENSE_CREATOR)
        }

        request.name?.let { name = it }
        request.amount?.let { amount = it }
        request.category?.let { category = it }
        request.description?.let { description = it }
        request.expenseDate?.let { expenseDate = it }
    }

    fun delete(requesterId: UUID) {
        if (creator.id != requesterId) {
            throw ApplicationException(ErrorCode.NOT_EXPENSE_CREATOR)
        }

        if (activity.status != ActivityStatus.ACTIVE) {
            throw ApplicationException(ErrorCode.INACTIVE_ACTIVITY)
        }

        isDeleted = true
        deletedAt = Instant.now()
    }
}