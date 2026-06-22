package com.aiexpensetracker.expense_tracker.dashboard;
import com.aiexpensetracker.expense_tracker.ai.dto.InsightResponse;
import com.aiexpensetracker.expense_tracker.dashboard.dto.SummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam(defaultValue = "")  String month) {
        YearMonth yearMonth = month.isEmpty()
                ? YearMonth.now()
                : YearMonth.parse(month);
        return ResponseEntity.ok(
                dashboardService.getMonthlySummary(yearMonth));
    }

    @GetMapping("/insights")
    public ResponseEntity<InsightResponse> getInsights(
            @RequestParam(defaultValue = "") String month) {
        YearMonth yearMonth = month.isEmpty()
                ? YearMonth.now()
                : YearMonth.parse(month);
        return ResponseEntity.ok(
                dashboardService.getInsights(yearMonth));
    }
}

