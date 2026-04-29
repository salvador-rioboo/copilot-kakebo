package com.kakebo.service;

import com.kakebo.dto.CreateExpenseDTO;
import com.kakebo.entity.Category;
import com.kakebo.entity.Expense;
import com.kakebo.exception.InvalidExpenseException;
import com.kakebo.exception.ResourceNotFoundException;
import com.kakebo.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExpenseServiceTest {
    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    private CreateExpenseDTO validExpenseDTO;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        validExpenseDTO = new CreateExpenseDTO(
            "Groceries",
            new BigDecimal("125.50"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        expense = new Expense(
            "Groceries",
            new BigDecimal("125.50"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
    }

    @Test
    void testCreateValidExpense() {
        var result = expenseService.create(validExpenseDTO);

        assertNotNull(result);
        assertEquals("Groceries", result.description());
        assertEquals(new BigDecimal("125.50"), result.amount());
    }

    @Test
    void testCreateExpenseWithNegativeAmountThrowsException() {
        CreateExpenseDTO invalidDTO = new CreateExpenseDTO(
            "Invalid",
            new BigDecimal("-10"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        assertThrows(InvalidExpenseException.class, () -> expenseService.create(invalidDTO));
    }

    @Test
    void testCreateExpenseWithBlankDescriptionThrowsException() {
        CreateExpenseDTO invalidDTO = new CreateExpenseDTO(
            "",
            new BigDecimal("100"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        assertThrows(InvalidExpenseException.class, () -> expenseService.create(invalidDTO));
    }

    @Test
    void testGetByIdReturnsExpense() {
        var created = expenseService.create(validExpenseDTO);
        var result = expenseService.getById(created.id());

        assertNotNull(result);
        assertEquals("Groceries", result.description());
    }

    @Test
    void testGetByIdThrowsExceptionWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> expenseService.getById(999L));
    }

    @Test
    void testDeleteRemovesExpense() {
        var created = expenseService.create(validExpenseDTO);

        expenseService.delete(created.id());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.getById(created.id()));
    }

    @Test
    void testDeleteThrowsExceptionWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> expenseService.delete(999L));
    }
}
