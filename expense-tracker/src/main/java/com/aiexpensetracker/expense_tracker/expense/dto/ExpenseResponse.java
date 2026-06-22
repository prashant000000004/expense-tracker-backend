package com.aiexpensetracker.expense_tracker.expense.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private LocalDate date;
    private String notes;
    private String receiptImageUrl;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
}
