package com.aiexpensetracker.expense_tracker.expense.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotNull(message = "Date is required")
    private LocalDate date;
    private String notes;
    private String receiptImageUrl;
    @NotNull(message = "Category is required")
    private Long categoryId;

}
