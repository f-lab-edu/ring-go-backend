package com.ringgo.domain.expense.entity

import com.ringgo.domain.activity.entity.ExpenseActivity
import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

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
    var expenseDate: Instant
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

    fun update(name: String?, amount: BigDecimal?, category: ExpenseCategory?,
               description: String?, expenseDate: Instant?) {
        name?.let { this.name = it }
        amount?.let { this.amount = it }
        category?.let { this.category = it }
        description?.let { this.description = it }
        expenseDate?.let { this.expenseDate = it }
    }

    fun markAsDeleted() {
        isDeleted = true
        deletedAt = Instant.now()
    }
}