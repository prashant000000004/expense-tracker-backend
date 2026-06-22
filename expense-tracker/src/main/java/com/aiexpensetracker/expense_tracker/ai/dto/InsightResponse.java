package com.aiexpensetracker.expense_tracker.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {
    private String summary;              // "You spent ₹12,450 this month"
    private List<String> insights;       // ["Food spending up 40%", ...]
    private List<String> suggestions;    // ["Try cooking at home", ...]
    private String savingsAdvice;        // personalized saving tip
}
