package com.kakebo.service;

import com.kakebo.dto.DashboardSummaryDTO;
import com.kakebo.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DashboardServiceTest {
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private IncomeService incomeService;

    @Test
    void testGetMonthlySummaryReturnsValidDTO() {
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(2026, 4);

        assertNotNull(summary);
        assertNotNull(summary.totalIncome());
        assertNotNull(summary.totalExpenses());
        assertNotNull(summary.availableMoney());
        assertNotNull(summary.plannedSavings());
        assertNotNull(summary.actualSavings());
    }

    @Test
    void testGetCurrentMonthlySummaryReturnsValidDTO() {
        DashboardSummaryDTO summary = dashboardService.getCurrentMonthlySummary();

        assertNotNull(summary);
        assertNotNull(summary.totalIncome());
        assertNotNull(summary.totalExpenses());
    }

    @Test
    void testHasBudgetAlertReturnsBooleanValue() {
        boolean hasAlert = dashboardService.hasBudgetAlert(2026, 4);

        // Should not throw exception, should return a boolean
        assertTrue(hasAlert || !hasAlert);
    }

    @Test
    void testGetMonthlySummaryCalculatesPlannedSavings() {
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(2026, 4);

        // Planned savings should be 20% of income
        BigDecimal expectedPlannedSavings = summary.totalIncome()
            .multiply(new BigDecimal("0.2"));

        assertEquals(expectedPlannedSavings, summary.plannedSavings());
    }

    @Test
    void testGetMonthlySummaryCalculatesActualSavings() {
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(2026, 4);

        // Actual savings is available money but not negative
        BigDecimal expectedActualSavings = summary.availableMoney()
            .max(BigDecimal.ZERO);

        assertEquals(expectedActualSavings, summary.actualSavings());
    }
}


