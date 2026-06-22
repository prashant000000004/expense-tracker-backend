package com.aiexpensetracker.expense_tracker.ai.dto;


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
public class ReceiptData {
    private String title;
    private BigDecimal amount;
    private LocalDate date;
    private String suggestedCategory;
    private String notes;
    private boolean success;
    private String errorMessage;
}
