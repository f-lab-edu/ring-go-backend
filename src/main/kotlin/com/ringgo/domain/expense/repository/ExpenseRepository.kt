package com.ringgo.domain.expense.repository

import com.ringgo.domain.expense.entity.Expense
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface ExpenseRepository : JpaRepository<Expense, Long> {
    @Query(
        """
        SELECT e 
        FROM Expense e 
        JOIN FETCH e.creator 
        WHERE e.activity.id = :activityId 
        AND e.isDeleted = false 
        ORDER BY e.expenseDate DESC, e.createdAt DESC
    """
    )
    fun findAllByActivityIdWithCreator(activityId: Long): List<Expense>

    @Query("""
        SELECT e 
        FROM Expense e 
        JOIN FETCH e.creator c
        WHERE e.activity.id = :activityId 
        AND e.isDeleted = false
        AND (:keyword IS NULL OR (
            LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            CAST(e.amount AS string) LIKE CONCAT('%', :keyword, '%')
        ))
        AND (:startDate IS NULL OR e.expenseDate >= :startDate)
        AND (:endDate IS NULL OR e.expenseDate <= :endDate)
    """)
    fun searchExpenses(
        @Param("activityId") activityId: Long,
        @Param("keyword") keyword: String?,
        @Param("startDate") startDate: LocalDate?,
        @Param("endDate") endDate: LocalDate?,
        pageable: Pageable
    ): Page<Expense>

    @Query("""
        SELECT SUM(e.amount)
        FROM Expense e 
        JOIN e.creator c
        WHERE e.activity.id = :activityId 
        AND e.isDeleted = false
        AND (:keyword IS NULL OR (
            LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            CAST(e.amount AS string) LIKE CONCAT('%', :keyword, '%')
        ))
        AND (:startDate IS NULL OR e.expenseDate >= :startDate)
        AND (:endDate IS NULL OR e.expenseDate <= :endDate)
    """)
    fun calculateTotalAmount(
        @Param("activityId") activityId: Long,
        @Param("keyword") keyword: String?,
        @Param("startDate") startDate: LocalDate?,
        @Param("endDate") endDate: LocalDate?
    ): BigDecimal?
}