package com.aiexpensetracker.expense_tracker.dashboard;

import com.aiexpensetracker.expense_tracker.ai.GeminiService;
import com.aiexpensetracker.expense_tracker.ai.dto.InsightResponse;
import com.aiexpensetracker.expense_tracker.dashboard.dto.CategorySummary;
import com.aiexpensetracker.expense_tracker.dashboard.dto.SummaryResponse;
import com.aiexpensetracker.expense_tracker.expense.Expense;
import com.aiexpensetracker.expense_tracker.expense.ExpenseRepository;
import com.aiexpensetracker.expense_tracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ExpenseRepository expenseRepository;
    private final GeminiService geminiService;

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }

    // ─── Monthly Summary ──────────────────────────────────
    public SummaryResponse getMonthlySummary(YearMonth month) {
        Long userId = getCurrentUser().getId();
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetween(userId, from, to);

        // total spent
        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // group by category
        Map<String, BigDecimal> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Expense::getAmount,
                                BigDecimal::add)
                ));

        List<CategorySummary> categorySummaries = byCategory.entrySet()
                .stream()
                .map(entry -> CategorySummary.builder()
                        .categoryName(entry.getKey())
                        .totalAmount(entry.getValue())
                        .percentage(total.compareTo(BigDecimal.ZERO) == 0
                                ? 0
                                : entry.getValue()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(total, 2,
                                        java.math.RoundingMode.HALF_UP)
                                .doubleValue())
                        .build())
                .sorted((a, b) -> b.getTotalAmount()
                        .compareTo(a.getTotalAmount()))
                .toList();

        return SummaryResponse.builder()
                .month(month.toString())
                .totalSpent(total)
                .expenseCount(expenses.size())
                .categorySummaries(categorySummaries)
                .build();
    }

    // ─── AI Insights ──────────────────────────────────────
    public InsightResponse getInsights(YearMonth month) {
        Long userId = getCurrentUser().getId();
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetween(userId, from, to);

        if (expenses.isEmpty()) {
            return InsightResponse.builder()
                    .summary("No expenses found for " + month)
                    .insights(List.of("Start adding expenses to get insights"))
                    .suggestions(List.of())
                    .savingsAdvice("")
                    .build();
        }

        // build spending summary string for Gemini
        StringBuilder spendingData = new StringBuilder();
        spendingData.append("Month: ").append(month).append("\n");

        expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO,
                                Expense::getAmount, BigDecimal::add)))
                .forEach((category, amount) ->
                        spendingData.append(category)
                                .append(": ₹")
                                .append(amount)
                                .append("\n"));

        return geminiService.generateInsights(spendingData.toString());
    }
}
