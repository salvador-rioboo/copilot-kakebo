package com.kakebo.service;

import com.kakebo.dto.DashboardSummaryDTO;
import com.kakebo.entity.Category;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Service
public class DashboardService {
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public DashboardService(ExpenseService expenseService, IncomeService incomeService) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
    }

    public DashboardSummaryDTO getMonthlySummary(int year, int month) {
        BigDecimal totalIncome = incomeService.getTotalByMonth(year, month);
        BigDecimal totalExpenses = expenseService.getTotalByMonth(year, month);
        BigDecimal availableMoney = totalIncome.subtract(totalExpenses);

        // Calculate planned savings (assuming 20% of income)
        BigDecimal plannedSavings = totalIncome.multiply(new BigDecimal("0.2"));

        // Actual savings is available money (after expenses)
        BigDecimal actualSavings = availableMoney.max(BigDecimal.ZERO);

        Map<Category, BigDecimal> expensesByCategory = expenseService.getTotalsByCategory(year, month);

        return new DashboardSummaryDTO(
            totalIncome,
            totalExpenses,
            availableMoney,
            plannedSavings,
            actualSavings,
            expensesByCategory
        );
    }

    public DashboardSummaryDTO getCurrentMonthlySummary() {
        YearMonth now = YearMonth.now();
        return getMonthlySummary(now.getYear(), now.getMonthValue());
    }

    public boolean hasBudgetAlert(int year, int month) {
        DashboardSummaryDTO summary = getMonthlySummary(year, month);
        return summary.actualSavings().compareTo(summary.plannedSavings()) < 0;
    }
}
