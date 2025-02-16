package com.ringgo.domain.expense.repository

import com.ringgo.domain.expense.entity.Expense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ExpenseRepository : JpaRepository<Expense, Long> {
    @Query("""
        SELECT e 
        FROM Expense e 
        JOIN FETCH e.creator 
        WHERE e.activity.id = :activityId 
        AND e.isDeleted = false 
        ORDER BY e.expenseDate DESC, e.createdAt DESC
    """)
    fun findAllByActivityIdWithCreator(activityId: Long): List<Expense>
}