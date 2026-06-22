package com.aiexpensetracker.expense_tracker.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    // for dashboard — get all expenses in a date range
    List<Expense> findByUserIdAndDateBetween(
            Long userId, LocalDate from, LocalDate to);

    // for dashboard — sum total spent
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :from AND :to")
    BigDecimal sumByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);


    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.category.id = :categoryId " +
            "AND e.date BETWEEN :from AND :to")
    BigDecimal sumByUserIdAndCategoryIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

}
