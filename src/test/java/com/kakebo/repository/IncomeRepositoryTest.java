package com.kakebo.repository;

import com.kakebo.entity.Income;
import com.kakebo.entity.IncomeType;
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
class IncomeRepositoryTest {
    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Income income1;
    private Income income2;
    private Income income3;

    @BeforeEach
    void setUp() {
        incomeRepository.deleteAll();

        // Create test incomes for April 2026
        income1 = new Income(
            "Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.of(2026, 4, 1)
        );

        income2 = new Income(
            "Bonus",
            new BigDecimal("500.00"),
            IncomeType.EXTRA,
            false,
            LocalDate.of(2026, 4, 15)
        );

        income3 = new Income(
            "Freelance Work",
            new BigDecimal("800.00"),
            IncomeType.EXTRA,
            false,
            LocalDate.of(2026, 4, 20)
        );

        incomeRepository.save(income1);
        incomeRepository.save(income2);
        incomeRepository.save(income3);
        testEntityManager.flush();
    }

    @Test
    void testFindByMonthReturnsIncomesForGivenMonth() {
        List<Income> result = incomeRepository.findByMonth(2026, 4);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testFindByMonthReturnsEmptyListWhenNoIncomesInMonth() {
        List<Income> result = incomeRepository.findByMonth(2026, 5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByMonthAndTypeReturnsFilteredIncomes() {
        List<Income> result = incomeRepository.findByMonthAndType(2026, 4, IncomeType.EXTRA);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getType() == IncomeType.EXTRA));
    }

    @Test
    void testFindByMonthAndTypeReturnsEmptyWhenNoTypeMatch() {
        List<Income> result = incomeRepository.findByMonthAndType(2026, 3, IncomeType.PRINCIPAL);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTotalByMonthReturnsCorrectSum() {
        BigDecimal result = incomeRepository.getTotalByMonth(2026, 4);

        assertNotNull(result);
        // 3000.00 + 500.00 + 800.00 = 4300.00
        assertEquals(new BigDecimal("4300.00"), result);
    }

    @Test
    void testGetTotalByMonthReturnsZeroWhenNoIncomes() {
        BigDecimal result = incomeRepository.getTotalByMonth(2026, 5);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testGetTotalByTypeAndMonthReturnsCorrectSum() {
        BigDecimal result = incomeRepository.getTotalByTypeAndMonth(2026, 4, IncomeType.EXTRA);

        assertNotNull(result);
        // 500.00 + 800.00 = 1300.00
        assertEquals(new BigDecimal("1300.00"), result);
    }

    @Test
    void testGetTotalByTypeAndMonthReturnsZeroWhenNoTypeMatch() {
        BigDecimal result = incomeRepository.getTotalByTypeAndMonth(2026, 3, IncomeType.PRINCIPAL);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testSaveAndRetrieveIncome() {
        Income newIncome = new Income(
            "Gift",
            new BigDecimal("200.00"),
            IncomeType.EXTRA,
            false,
            LocalDate.of(2026, 4, 25)
        );

        Income saved = incomeRepository.save(newIncome);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);

        Income retrieved = incomeRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("Gift", retrieved.getDescription());
    }

    @Test
    void testDeleteIncomeRemovesFromRepository() {
        Long id = income1.getId();

        incomeRepository.delete(income1);
        testEntityManager.flush();

        assertTrue(incomeRepository.findById(id).isEmpty());
    }

    @Test
    void testUpdateIncomeChangesValues() {
        Long id = income2.getId();
        income2.setAmount(new BigDecimal("750.00"));
        income2.setDescription("Updated Bonus");

        incomeRepository.save(income2);
        testEntityManager.flush();

        Income updated = incomeRepository.findById(id).orElse(null);
        assertNotNull(updated);
        assertEquals("Updated Bonus", updated.getDescription());
        assertEquals(new BigDecimal("750.00"), updated.getAmount());
    }

    @Test
    void testIncomeTimestampsAreSet() {
        Income newIncome = new Income(
            "Test Income",
            new BigDecimal("100.00"),
            IncomeType.PRINCIPAL,
            false,
            LocalDate.now()
        );

        Income saved = incomeRepository.save(newIncome);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testFindByMonthAndTypePrincipalReturnsOnlyPrincipal() {
        List<Income> result = incomeRepository.findByMonthAndType(2026, 4, IncomeType.PRINCIPAL);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(IncomeType.PRINCIPAL, result.get(0).getType());
    }
}
