package com.aiexpensetracker.expense_tracker.expense;


import com.aiexpensetracker.expense_tracker.category.Category;
import com.aiexpensetracker.expense_tracker.category.CategoryRepository;
import com.aiexpensetracker.expense_tracker.expense.dto.ExpenseRequest;
import com.aiexpensetracker.expense_tracker.expense.dto.ExpenseResponse;
import com.aiexpensetracker.expense_tracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.LocalDate;

import static com.aiexpensetracker.expense_tracker.expense.ExpenseSpecification.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }

    // ─── Get Paginated + Filtered Expenses ────────────────
    public ExpensePageResponse getExpenses(
            int page, int size, Long categoryId,
            LocalDate from, LocalDate to, String search) {

        Long userId = getCurrentUser().getId();

        // combine all filters dynamically — only non-null ones apply
        Specification<Expense> spec = Specification
                .where(belongsToUser(userId))
                .and(hasCategoryId(categoryId))
                .and(dateBetween(from, to))
                .and(titleContains(search));

        var pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "date"));

        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        return ExpensePageResponse.builder()
                .expenses(result.getContent().stream()
                        .map(this::toResponse).toList())
                .currentPage(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalItems(result.getTotalElements())
                .hasNext(result.hasNext())
                .build();
    }

    // ─── Get Single Expense ───────────────────────────────
    public ExpenseResponse getExpenseById(Long id) {
        var expense = expenseRepository
                .findByIdAndUserId(id, getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        return toResponse(expense);
    }

    // ─── Create Expense ────────────────────────────────────
    public ExpenseResponse createExpense(ExpenseRequest request) {
        User user = getCurrentUser();

        var category = categoryRepository
                .findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        var expense = Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .date(request.getDate())
                .notes(request.getNotes())
                .receiptImageUrl(request.getReceiptImageUrl())
                .category(category)
                .user(user)
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    // ─── Update Expense ────────────────────────────────────
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        User user = getCurrentUser();

        var expense = expenseRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        var category = categoryRepository
                .findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setNotes(request.getNotes());
        expense.setCategory(category);

        return toResponse(expenseRepository.save(expense));
    }

    // ─── Delete Expense ────────────────────────────────────
    public void deleteExpense(Long id) {
        var expense = expenseRepository
                .findByIdAndUserId(id, getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expenseRepository.delete(expense);
    }

    // ─── Map Entity to Response ───────────────────────────
    private ExpenseResponse toResponse(Expense expense) {
        Category category = expense.getCategory();
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .notes(expense.getNotes())
                .receiptImageUrl(expense.getReceiptImageUrl())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .categoryIcon(category.getIcon())
                .categoryColor(category.getColor())
                .build();
    }
}
