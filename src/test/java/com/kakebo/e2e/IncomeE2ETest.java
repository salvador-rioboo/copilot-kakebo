package com.kakebo.e2e;

import com.kakebo.dto.CreateIncomeDTO;
import com.kakebo.dto.IncomeResponseDTO;
import com.kakebo.entity.IncomeType;
import com.kakebo.repository.IncomeRepository;
import com.kakebo.service.IncomeService;
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
class IncomeE2ETest {
    @Autowired
    private IncomeService incomeService;

    @Autowired
    private IncomeRepository incomeRepository;

    @BeforeEach
    void setUp() {
        incomeRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        incomeRepository.deleteAll();
    }

    @Test
    void testCompleteIncomeWorkflow_CreateRetrieveUpdateDelete() {
        // Step 1: Create an income
        CreateIncomeDTO createDTO = new CreateIncomeDTO(
            "Monthly Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        IncomeResponseDTO created = incomeService.create(createDTO);
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("Monthly Salary", created.description());

        // Step 2: Retrieve the income by ID
        Long incomeId = created.id();
        IncomeResponseDTO retrieved = incomeService.getById(incomeId);

        assertNotNull(retrieved);
        assertEquals("Monthly Salary", retrieved.description());
        assertEquals(new BigDecimal("3000.00"), retrieved.amount());

        // Step 3: Update the income
        CreateIncomeDTO updateDTO = new CreateIncomeDTO(
            "Updated Salary",
            new BigDecimal("3500.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        IncomeResponseDTO updated = incomeService.update(incomeId, updateDTO);

        assertNotNull(updated);
        assertEquals("Updated Salary", updated.description());
        assertEquals(new BigDecimal("3500.00"), updated.amount());

        // Step 4: Delete the income
        incomeService.delete(incomeId);

        // Step 5: Verify deletion
        assertTrue(incomeRepository.findById(incomeId).isEmpty());
    }

    @Test
    void testMultipleIncomeCreation_FilterByMonthAndType() {
        // Create multiple principal incomes
        for (int i = 0; i < 2; i++) {
            CreateIncomeDTO dto = new CreateIncomeDTO(
                "Salary " + i,
                new BigDecimal("3000.00"),
                IncomeType.PRINCIPAL,
                true,
                LocalDate.now()
            );
            incomeService.create(dto);
        }

        // Create multiple extra incomes
        for (int i = 0; i < 3; i++) {
            CreateIncomeDTO dto = new CreateIncomeDTO(
                "Bonus " + i,
                new BigDecimal("500.00"),
                IncomeType.EXTRA,
                false,
                LocalDate.now()
            );
            incomeService.create(dto);
        }

        // Filter by type
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        List<IncomeResponseDTO> principalIncomes = incomeService.getByMonthAndType(year, month, IncomeType.PRINCIPAL);
        List<IncomeResponseDTO> extraIncomes = incomeService.getByMonthAndType(year, month, IncomeType.EXTRA);

        assertEquals(2, principalIncomes.size());
        assertEquals(3, extraIncomes.size());
    }

    @Test
    void testIncomeAggregation_CalculateTotalByMonth() {
        // Create multiple incomes in the same month
        CreateIncomeDTO[] dtos = {
            new CreateIncomeDTO("Salary", new BigDecimal("3000.00"), IncomeType.PRINCIPAL, true, LocalDate.now()),
            new CreateIncomeDTO("Bonus", new BigDecimal("500.00"), IncomeType.EXTRA, false, LocalDate.now()),
            new CreateIncomeDTO("Gift", new BigDecimal("200.00"), IncomeType.EXTRA, false, LocalDate.now())
        };

        for (CreateIncomeDTO dto : dtos) {
            incomeService.create(dto);
        }

        // Calculate total
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        BigDecimal total = incomeService.getTotalByMonth(year, month);

        // 3000.00 + 500.00 + 200.00 = 3700.00
        assertEquals(new BigDecimal("3700.00"), total);
    }

    @Test
    void testIncomeRetrieval_GetAllByMonth() {
        // Create income for this month
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        CreateIncomeDTO dto = new CreateIncomeDTO(
            "This Month Income",
            new BigDecimal("2500.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(dto);

        // Get all incomes for current month
        List<IncomeResponseDTO> incomes = incomeService.getAllByMonth(year, month);

        assertNotNull(incomes);
        assertFalse(incomes.isEmpty());
        assertTrue(incomes.stream().anyMatch(i -> "This Month Income".equals(i.description())));
    }

    @Test
    void testIncomeFiltering_ByMonthAndType() {
        // Create income with specific type
        CreateIncomeDTO principalDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        incomeService.create(principalDTO);

        // Filter by type
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        List<IncomeResponseDTO> principalIncomes = incomeService.getByMonthAndType(year, month, IncomeType.PRINCIPAL);

        assertEquals(1, principalIncomes.size());
        assertEquals(IncomeType.PRINCIPAL, principalIncomes.get(0).type());
    }

    @Test
    void testTypeAggregation_CalculateTotalByMonthAndType() {
        // Create multiple extra incomes
        for (int i = 0; i < 3; i++) {
            CreateIncomeDTO dto = new CreateIncomeDTO(
                "Extra Income " + i,
                new BigDecimal("200.00"),
                IncomeType.EXTRA,
                false,
                LocalDate.now()
            );
            incomeService.create(dto);
        }

        // Calculate total for the type
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        BigDecimal total = incomeService.getTotalByMonthAndType(year, month, IncomeType.EXTRA);

        // 200.00 * 3 = 600.00
        assertEquals(new BigDecimal("600.00"), total);
    }

    @Test
    void testIncomeNonExistentId_ThrowsException() {
        // Try to get non-existent income
        assertThrows(Exception.class, () -> incomeService.getById(999L));
    }

    @Test
    void testRecurringIncomeCreation() {
        CreateIncomeDTO recurringDTO = new CreateIncomeDTO(
            "Monthly Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,  // isRecurring
            LocalDate.now()
        );

        IncomeResponseDTO created = incomeService.create(recurringDTO);

        assertNotNull(created);
        assertTrue(created.isRecurring());
        assertEquals(IncomeType.PRINCIPAL, created.type());
    }

    @Test
    void testExtraIncomeCreation() {
        CreateIncomeDTO extraDTO = new CreateIncomeDTO(
            "Freelance Project",
            new BigDecimal("500.00"),
            IncomeType.EXTRA,
            false,
            LocalDate.now()
        );

        IncomeResponseDTO created = incomeService.create(extraDTO);

        assertNotNull(created);
        assertFalse(created.isRecurring());
        assertEquals(IncomeType.EXTRA, created.type());
    }
}
