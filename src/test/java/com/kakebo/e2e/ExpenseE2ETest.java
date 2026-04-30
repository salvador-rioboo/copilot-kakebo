package com.kakebo.e2e;

import com.kakebo.dto.CreateExpenseDTO;
import com.kakebo.dto.ExpenseResponseDTO;
import com.kakebo.entity.Category;
import com.kakebo.repository.ExpenseRepository;
import com.kakebo.service.ExpenseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExpenseE2ETest {
    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        expenseRepository.deleteAll();
    }

    @Test
    void testCompleteExpenseWorkflow_CreateRetrieveUpdateDelete() {
        // Step 1: Create an expense
        CreateExpenseDTO createDTO = new CreateExpenseDTO(
            "Groceries",
            new BigDecimal("125.50"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        ExpenseResponseDTO created = expenseService.create(createDTO);
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("Groceries", created.description());

        // Step 2: Retrieve the expense by ID
        Long expenseId = created.id();
        ExpenseResponseDTO retrieved = expenseService.getById(expenseId);

        assertNotNull(retrieved);
        assertEquals("Groceries", retrieved.description());
        assertEquals(new BigDecimal("125.50"), retrieved.amount());

        // Step 3: Update the expense
        CreateExpenseDTO updateDTO = new CreateExpenseDTO(
            "Updated Groceries",
            new BigDecimal("150.75"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        ExpenseResponseDTO updated = expenseService.update(expenseId, updateDTO);

        assertNotNull(updated);
        assertEquals("Updated Groceries", updated.description());
        assertEquals(new BigDecimal("150.75"), updated.amount());

        // Step 4: Delete the expense
        expenseService.delete(expenseId);

        // Step 5: Verify deletion
        assertTrue(expenseRepository.findById(expenseId).isEmpty());
    }

    @Test
    void testMultipleExpenseCreation_FilterByMonthAndCategory() {
        // Create multiple expenses
        for (int i = 0; i < 3; i++) {
            CreateExpenseDTO dto = new CreateExpenseDTO(
                "Survival Expense " + i,
                new BigDecimal("50.00"),
                Category.SURVIVAL,
                false,
                false,
                LocalDate.now()
            );
            expenseService.create(dto);
        }

        for (int i = 0; i < 2; i++) {
            CreateExpenseDTO dto = new CreateExpenseDTO(
                "Entertainment Expense " + i,
                new BigDecimal("30.00"),
                Category.ENTERTAINMENT,
                false,
                false,
                LocalDate.now()
            );
            expenseService.create(dto);
        }

        // Filter by current month and category
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        List<ExpenseResponseDTO> survivalExpenses = expenseService.getByMonthAndCategory(year, month, Category.SURVIVAL);
        List<ExpenseResponseDTO> entertainmentExpenses = expenseService.getByMonthAndCategory(year, month, Category.ENTERTAINMENT);

        assertEquals(3, survivalExpenses.size());
        assertEquals(2, entertainmentExpenses.size());
    }

    @Test
    void testExpenseAggregation_CalculateTotalByMonth() {
        // Create multiple expenses in the same month
        CreateExpenseDTO[] dtos = {
            new CreateExpenseDTO("Expense 1", new BigDecimal("100.00"), Category.SURVIVAL, false, false, LocalDate.now()),
            new CreateExpenseDTO("Expense 2", new BigDecimal("50.00"), Category.ENTERTAINMENT, false, false, LocalDate.now()),
            new CreateExpenseDTO("Expense 3", new BigDecimal("75.50"), Category.CULTURE, false, false, LocalDate.now())
        };

        for (CreateExpenseDTO dto : dtos) {
            expenseService.create(dto);
        }

        // Calculate total
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        BigDecimal total = expenseService.getTotalByMonth(year, month);

        // 100.00 + 50.00 + 75.50 = 225.50
        assertEquals(new BigDecimal("225.50"), total);
    }

    @Test
    void testExpenseRetrieval_GetAllByMonth() {
        // Create expenses for this month
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        CreateExpenseDTO dto1 = new CreateExpenseDTO(
            "This Month Expense",
            new BigDecimal("100.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(dto1);

        // Get all expenses for current month
        List<ExpenseResponseDTO> expenses = expenseService.getAllByMonth(year, month);

        assertNotNull(expenses);
        assertFalse(expenses.isEmpty());
        assertTrue(expenses.stream().anyMatch(e -> "This Month Expense".equals(e.description())));
    }

    @Test
    void testExpenseFiltering_ByMonthAndCategory() {
        // Create expenses with different categories
        CreateExpenseDTO cultureExpense = new CreateExpenseDTO(
            "Book",
            new BigDecimal("25.00"),
            Category.CULTURE,
            false,
            false,
            LocalDate.now()
        );
        expenseService.create(cultureExpense);

        // Filter by category
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        List<ExpenseResponseDTO> cultureExpenses = expenseService.getByMonthAndCategory(year, month, Category.CULTURE);

        assertEquals(1, cultureExpenses.size());
        assertEquals(Category.CULTURE, cultureExpenses.get(0).category());
    }

    @Test
    void testCategoryAggregation_CalculateTotalByMonthAndCategory() {
        // Create multiple expenses in the same category
        for (int i = 0; i < 3; i++) {
            CreateExpenseDTO dto = new CreateExpenseDTO(
                "Survival " + i,
                new BigDecimal("100.00"),
                Category.SURVIVAL,
                false,
                false,
                LocalDate.now()
            );
            expenseService.create(dto);
        }

        // Calculate total for the category
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        BigDecimal total = expenseService.getTotalByMonthAndCategory(year, month, Category.SURVIVAL);

        // 100.00 * 3 = 300.00
        assertEquals(new BigDecimal("300.00"), total);
    }

    @Test
    void testExpenseNonExistentId_ThrowsException() {
        // Try to get non-existent expense
        assertThrows(Exception.class, () -> expenseService.getById(999L));
    }

    @Test
    void testFixedExpenseCreation() {
        CreateExpenseDTO fixedExpenseDTO = new CreateExpenseDTO(
            "Monthly Rent",
            new BigDecimal("1200.00"),
            Category.SURVIVAL,
            true,  // isFixed
            true,  // isRecurring
            LocalDate.now()
        );

        ExpenseResponseDTO created = expenseService.create(fixedExpenseDTO);

        assertNotNull(created);
        assertTrue(created.isFixed());
        assertTrue(created.isRecurring());
    }
}
