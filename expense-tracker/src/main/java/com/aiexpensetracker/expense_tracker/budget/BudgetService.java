package com.aiexpensetracker.expense_tracker.budget;
import com.aiexpensetracker.expense_tracker.budget.dto.BudgetRequest;
import com.aiexpensetracker.expense_tracker.budget.dto.BudgetResponse;
import com.aiexpensetracker.expense_tracker.category.Category;
import com.aiexpensetracker.expense_tracker.category.CategoryRepository;
import com.aiexpensetracker.expense_tracker.expense.ExpenseRepository;
import com.aiexpensetracker.expense_tracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }

    // ─── Get All Budgets For a Month ──────────────────────
    public List<BudgetResponse> getBudgets(String month) {
        Long userId = getCurrentUser().getId();
        return budgetRepository
                .findByUserIdAndMonth(userId, month)
                .stream()
                .map(b -> toResponse(b, userId, month))
                .toList();
    }

    // ─── Create Budget ────────────────────────────────────
    public BudgetResponse createBudget(BudgetRequest request) {
        User user = getCurrentUser();

        // one budget per category per month — no duplicates
        budgetRepository.findByUserIdAndCategoryIdAndMonth(
                        user.getId(), request.getCategoryId(), request.getMonth())
                .ifPresent(b -> {
                    throw new RuntimeException(
                            "Budget already exists for this category and month");
                });

        Category category = categoryRepository
                .findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        var budget = Budget.builder()
                .amount(request.getAmount())
                .month(request.getMonth())
                .category(category)
                .user(user)
                .build();

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved, user.getId(), request.getMonth());
    }

    // ─── Update Budget ────────────────────────────────────
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        User user = getCurrentUser();

        var budget = budgetRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Budget not found"));

        budget.setAmount(request.getAmount());
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved, user.getId(), budget.getMonth());
    }

    // ─── Delete Budget ────────────────────────────────────
    public void deleteBudget(Long id) {
        var budget = budgetRepository
                .findByIdAndUserId(id, getCurrentUser().getId())
                .orElseThrow(() ->
                        new RuntimeException("Budget not found"));
        budgetRepository.delete(budget);
    }

    // ─── Map to Response with Calculated Fields ───────────
    private BudgetResponse toResponse(
            Budget budget, Long userId, String month) {

        // calculate how much was spent in this category this month
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        BigDecimal spent = expenseRepository
                .sumByUserIdAndCategoryIdAndDateBetween(
                        userId,
                        budget.getCategory().getId(),
                        from, to);

        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remaining = budgetAmount.subtract(spent);

        double percentage = budgetAmount.compareTo(BigDecimal.ZERO) == 0
                ? 0
                : spent.multiply(BigDecimal.valueOf(100))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP)
                .doubleValue();

        return BudgetResponse.builder()
                .id(budget.getId())
                .amount(budgetAmount)
                .spent(spent)
                .remaining(remaining)
                .percentage(percentage)
                .month(month)
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .categoryIcon(budget.getCategory().getIcon())
                .categoryColor(budget.getCategory().getColor())
                .isExceeded(spent.compareTo(budgetAmount) > 0)
                .build();
    }
}
