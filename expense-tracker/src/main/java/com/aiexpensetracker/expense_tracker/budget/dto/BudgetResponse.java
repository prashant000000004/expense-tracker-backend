package com.aiexpensetracker.expense_tracker.budget.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {
    private Long id;
    private BigDecimal amount;
    private BigDecimal spent;           // how much spent so far
    private BigDecimal remaining;       // amount - spent
    private double percentage;          // spent/amount * 100
    private String month;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private boolean isExceeded;         // spent > amount
}