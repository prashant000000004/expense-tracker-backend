package com.aiexpensetracker.expense_tracker.budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository  extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndMonth(Long userId, String month);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    Optional<Budget> findByUserIdAndCategoryIdAndMonth(
            Long userId, Long categoryId, String month);
}