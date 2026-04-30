package com.kakebo.repository;

import com.kakebo.entity.Category;
import com.kakebo.entity.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExpenseRepositoryTest {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Expense expense1;
    private Expense expense2;
    private Expense expense3;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        // Create test expenses for April 2026
        expense1 = new Expense(
            "Groceries",
            new BigDecimal("125.50"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.of(2026, 4, 10)
        );

        expense2 = new Expense(
            "Movie",
            new BigDecimal("15.00"),
            Category.ENTERTAINMENT,
            false,
            false,
            LocalDate.of(2026, 4, 15)
        );

        expense3 = new Expense(
            "Rent",
            new BigDecimal("1200.00"),
            Category.SURVIVAL,
            true,
            true,
            LocalDate.of(2026, 4, 1)
        );

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        testEntityManager.flush();
    }

    @Test
    void testFindByMonthReturnsExpensesForGivenMonth() {
        List<Expense> result = expenseRepository.findByMonth(2026, 4);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testFindByMonthReturnsEmptyListWhenNoExpensesInMonth() {
        List<Expense> result = expenseRepository.findByMonth(2026, 5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByMonthAndCategoryReturnsFilteredExpenses() {
        List<Expense> result = expenseRepository.findByMonthAndCategory(2026, 4, Category.SURVIVAL);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getCategory() == Category.SURVIVAL));
    }

    @Test
    void testFindByMonthAndCategoryReturnsEmptyWhenNoCategoryMatch() {
        List<Expense> result = expenseRepository.findByMonthAndCategory(2026, 4, Category.CULTURE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTotalByMonthReturnsCorrectSum() {
        BigDecimal result = expenseRepository.getTotalByMonth(2026, 4);

        assertNotNull(result);
        // 125.50 + 15.00 + 1200.00 = 1340.50
        assertEquals(new BigDecimal("1340.50"), result);
    }

    @Test
    void testGetTotalByMonthReturnsZeroWhenNoExpenses() {
        BigDecimal result = expenseRepository.getTotalByMonth(2026, 5);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testGetTotalByCategoryAndMonthReturnsCorrectSum() {
        BigDecimal result = expenseRepository.getTotalByCategoryAndMonth(2026, 4, Category.SURVIVAL);

        assertNotNull(result);
        // 125.50 + 1200.00 = 1325.50
        assertEquals(new BigDecimal("1325.50"), result);
    }

    @Test
    void testGetTotalByCategoryAndMonthReturnsZeroWhenNoCategoryMatch() {
        BigDecimal result = expenseRepository.getTotalByCategoryAndMonth(2026, 4, Category.EXTRAS);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testSaveAndRetrieveExpense() {
        Expense newExpense = new Expense(
            "Books",
            new BigDecimal("45.00"),
            Category.CULTURE,
            false,
            false,
            LocalDate.of(2026, 4, 20)
        );

        Expense saved = expenseRepository.save(newExpense);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);

        Expense retrieved = expenseRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("Books", retrieved.getDescription());
    }

    @Test
    void testDeleteExpenseRemovesFromRepository() {
        Long id = expense1.getId();

        expenseRepository.delete(expense1);
        testEntityManager.flush();

        assertTrue(expenseRepository.findById(id).isEmpty());
    }

    @Test
    void testUpdateExpenseChangesValues() {
        Long id = expense1.getId();
        expense1.setAmount(new BigDecimal("200.00"));
        expense1.setDescription("Updated Groceries");

        expenseRepository.save(expense1);
        testEntityManager.flush();

        Expense updated = expenseRepository.findById(id).orElse(null);
        assertNotNull(updated);
        assertEquals("Updated Groceries", updated.getDescription());
        assertEquals(new BigDecimal("200.00"), updated.getAmount());
    }

    @Test
    void testExpenseTimestampsAreSet() {
        Expense newExpense = new Expense(
            "Test",
            new BigDecimal("10.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        Expense saved = expenseRepository.save(newExpense);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }
}
