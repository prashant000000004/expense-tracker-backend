package com.aiexpensetracker.expense_tracker.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummaryResponse {
    private String month;
    private BigDecimal totalSpent;
    private int expenseCount;
    private List<CategorySummary> categorySummaries;
}
