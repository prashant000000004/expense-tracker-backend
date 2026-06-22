package com.aiexpensetracker.expense_tracker.expense;


import com.aiexpensetracker.expense_tracker.expense.dto.ExpenseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpensePageResponse {
    private List<ExpenseResponse> expenses;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean hasNext;
}
