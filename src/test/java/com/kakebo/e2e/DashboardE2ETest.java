package com.kakebo.e2e;

import com.kakebo.dto.CreateExpenseDTO;
import com.kakebo.dto.CreateIncomeDTO;
import com.kakebo.dto.DashboardSummaryDTO;
import com.kakebo.entity.Category;
import com.kakebo.entity.IncomeType;
import com.kakebo.repository.ExpenseRepository;
import com.kakebo.repository.IncomeRepository;
import com.kakebo.service.DashboardService;
import com.kakebo.service.ExpenseService;
import com.kakebo.service.IncomeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DashboardE2ETest {
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        incomeRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        expenseRepository.deleteAll();
        incomeRepository.deleteAll();
    }

    @Test
    void testMonthlyDashboardSummary_WithIncomeAndExpenses() {
        // Create income for the month
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Monthly Salary",
            new BigDecimal("4000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(incomeDTO);

        // Create expenses for the month
        CreateExpenseDTO survivalDTO = new CreateExpenseDTO(
            "Rent",
            new BigDecimal("1200.00"),
            Category.SURVIVAL,
            true,
            true,
            LocalDate.now()
        );
        expenseService.create(survivalDTO);

        CreateExpenseDTO entertainmentDTO = new CreateExpenseDTO(
            "Movies",
            new BigDecimal("30.00"),
            Category.ENTERTAINMENT,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(entertainmentDTO);

        // Get dashboard summary for current month
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(year, month);

        assertNotNull(summary);
        assertEquals(new BigDecimal("4000.00"), summary.totalIncome());
        assertEquals(new BigDecimal("1230.00"), summary.totalExpenses());
        // availableMoney = totalIncome - totalExpenses = 4000.00 - 1230.00 = 2770.00
        assertEquals(new BigDecimal("2770.00"), summary.availableMoney());
    }

    @Test
    void testDashboardSummary_WithMultipleIncomes() {
        // Create principal income
        CreateIncomeDTO principalDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(principalDTO);

        // Create extra income
        CreateIncomeDTO extraDTO = new CreateIncomeDTO(
            "Bonus",
            new BigDecimal("500.00"),
            IncomeType.EXTRA,
            false,
            LocalDate.now()
        );
        incomeService.create(extraDTO);

        // Create expense
        CreateExpenseDTO expenseDTO = new CreateExpenseDTO(
            "Groceries",
            new BigDecimal("100.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(expenseDTO);

        // Get summary
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(year, month);

        assertNotNull(summary);
        // Total income = 3000.00 + 500.00 = 3500.00
        assertEquals(new BigDecimal("3500.00"), summary.totalIncome());
        assertEquals(new BigDecimal("100.00"), summary.totalExpenses());
    }

    @Test
    void testDashboardSummary_WithMultipleExpenses() {
        // Create income
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("5000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(incomeDTO);

        // Create multiple expenses in different categories
        CreateExpenseDTO[] expenses = {
            new CreateExpenseDTO("Rent", new BigDecimal("1200.00"), Category.SURVIVAL, true, true, LocalDate.now()),
            new CreateExpenseDTO("Utilities", new BigDecimal("150.00"), Category.SURVIVAL, false, false, LocalDate.now()),
            new CreateExpenseDTO("Movie", new BigDecimal("15.00"), Category.ENTERTAINMENT, false, false, LocalDate.now()),
            new CreateExpenseDTO("Book", new BigDecimal("20.00"), Category.CULTURE, false, false, LocalDate.now()),
            new CreateExpenseDTO("Unexpected", new BigDecimal("50.00"), Category.EXTRAS, false, false, LocalDate.now())
        };

        for (CreateExpenseDTO expense : expenses) {
            expenseService.create(expense);
        }

        // Get summary
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(year, month);

        assertNotNull(summary);
        assertEquals(new BigDecimal("5000.00"), summary.totalIncome());
        // Total expenses = 1200 + 150 + 15 + 20 + 50 = 1435.00
        assertEquals(new BigDecimal("1435.00"), summary.totalExpenses());
        // Available = 5000.00 - 1435.00 = 3565.00
        assertEquals(new BigDecimal("3565.00"), summary.availableMoney());
    }

    @Test
    void testDashboardSummary_EmptyMonth() {
        // Get summary for month with no data
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue() > 1 ? LocalDate.now().getMonthValue() - 1 : 12;

        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(year, month);

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.totalIncome());
        assertEquals(BigDecimal.ZERO, summary.totalExpenses());
        assertEquals(BigDecimal.ZERO, summary.availableMoney());
    }

    @Test
    void testDashboardSummary_CurrentMonth() {
        // Create data for current month
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(incomeDTO);

        CreateExpenseDTO expenseDTO = new CreateExpenseDTO(
            "Expenses",
            new BigDecimal("500.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(expenseDTO);

        // Get current month summary
        DashboardSummaryDTO currentSummary = dashboardService.getCurrentMonthlySummary();

        assertNotNull(currentSummary);
        assertEquals(new BigDecimal("3000.00"), currentSummary.totalIncome());
        assertEquals(new BigDecimal("500.00"), currentSummary.totalExpenses());
        assertEquals(new BigDecimal("2500.00"), currentSummary.availableMoney());
    }

    @Test
    void testBudgetCalculation_SavingsTarget() {
        // Create scenario: Income 4000, fixed expenses 1500, variable expenses 800
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("4000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(incomeDTO);

        // Fixed expenses
        CreateExpenseDTO fixedDTO = new CreateExpenseDTO(
            "Rent",
            new BigDecimal("1500.00"),
            Category.SURVIVAL,
            true,
            true,
            LocalDate.now()
        );
        expenseService.create(fixedDTO);

        // Variable expenses
        CreateExpenseDTO variableDTO = new CreateExpenseDTO(
            "Groceries",
            new BigDecimal("800.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(variableDTO);

        // Get summary
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(year, month);

        assertNotNull(summary);
        // Available for savings = 4000 - 1500 - 800 = 1700
        assertEquals(new BigDecimal("1700.00"), summary.availableMoney());
    }

    @Test
    void testDashboardWithHighExpenses_ExceedsIncome() {
        // Create scenario: Income 2000, expenses 3000 (deficit)
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Limited Salary",
            new BigDecimal("2000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(incomeDTO);

        CreateExpenseDTO expenseDTO = new CreateExpenseDTO(
            "High Expenses",
            new BigDecimal("3000.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(expenseDTO);

        // Get summary
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        DashboardSummaryDTO summary = dashboardService.getMonthlySummary(year, month);

        assertNotNull(summary);
        assertEquals(new BigDecimal("2000.00"), summary.totalIncome());
        assertEquals(new BigDecimal("3000.00"), summary.totalExpenses());
        // Negative available (deficit)
        assertEquals(new BigDecimal("-1000.00"), summary.availableMoney());
    }

    @Test
    void testDashboardCategoryBreakdown() {
        // Create income
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("4000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(incomeDTO);

        // Create expenses in different categories
        CreateExpenseDTO survival = new CreateExpenseDTO(
            "Rent",
            new BigDecimal("1500.00"),
            Category.SURVIVAL,
            true,
            true,
            LocalDate.now()
        );
        expenseService.create(survival);

        CreateExpenseDTO entertainment = new CreateExpenseDTO(
            "Entertainment",
            new BigDecimal("150.00"),
            Category.ENTERTAINMENT,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(entertainment);

        // Get category breakdowns
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        BigDecimal survivalTotal = expenseService.getTotalByMonthAndCategory(year, month, Category.SURVIVAL);
        BigDecimal entertainmentTotal = expenseService.getTotalByMonthAndCategory(year, month, Category.ENTERTAINMENT);

        assertEquals(new BigDecimal("1500.00"), survivalTotal);
        assertEquals(new BigDecimal("150.00"), entertainmentTotal);
    }
}
