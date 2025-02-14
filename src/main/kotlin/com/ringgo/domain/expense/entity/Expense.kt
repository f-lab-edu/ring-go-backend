package com.ringgo.domain.expense.entity

import com.ringgo.domain.activity.entity.ExpenseActivity
import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "expense")
@EntityListeners(AuditingEntityListener::class)
class Expense(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    val activity: ExpenseActivity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val category: ExpenseCategory,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column(nullable = false)
    val expenseDate: Instant,
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

    @Column(nullable = false)
    var isDeleted: Boolean = false

    @Column
    var deletedAt: Instant? = null
}