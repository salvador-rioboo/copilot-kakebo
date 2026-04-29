package com.kakebo.service;

import com.kakebo.dto.CreateIncomeDTO;
import com.kakebo.dto.IncomeResponseDTO;
import com.kakebo.entity.Income;
import com.kakebo.entity.IncomeType;
import com.kakebo.exception.InvalidIncomeException;
import com.kakebo.exception.ResourceNotFoundException;
import com.kakebo.repository.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IncomeServiceTest {
    @Autowired
    private IncomeService incomeService;

    @Autowired
    private IncomeRepository incomeRepository;

    private CreateIncomeDTO validIncomeDTO;
    private Income income;

    @BeforeEach
    void setUp() {
        incomeRepository.deleteAll();
        
        validIncomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        income = new Income(
            "Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
    }

    @Test
    void testCreateValidIncome() {
        IncomeResponseDTO result = incomeService.create(validIncomeDTO);

        assertNotNull(result);
        assertEquals("Salary", result.description());
        assertEquals(new BigDecimal("3000.00"), result.amount());
        assertEquals(IncomeType.PRINCIPAL, result.type());
    }

    @Test
    void testCreateIncomeWithNegativeAmountThrowsException() {
        CreateIncomeDTO invalidDTO = new CreateIncomeDTO(
            "Invalid",
            new BigDecimal("-100"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        assertThrows(InvalidIncomeException.class, () -> incomeService.create(invalidDTO));
    }

    @Test
    void testCreateIncomeWithZeroAmountThrowsException() {
        CreateIncomeDTO invalidDTO = new CreateIncomeDTO(
            "Invalid",
            new BigDecimal("0"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        assertThrows(InvalidIncomeException.class, () -> incomeService.create(invalidDTO));
    }

    @Test
    void testCreateIncomeWithBlankDescriptionThrowsException() {
        CreateIncomeDTO invalidDTO = new CreateIncomeDTO(
            "",
            new BigDecimal("1000"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        assertThrows(InvalidIncomeException.class, () -> incomeService.create(invalidDTO));
    }

    @Test
    void testGetByIdReturnsIncome() {
        IncomeResponseDTO created = incomeService.create(validIncomeDTO);
        IncomeResponseDTO result = incomeService.getById(created.id());

        assertNotNull(result);
        assertEquals("Salary", result.description());
    }

    @Test
    void testGetByIdThrowsExceptionWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> incomeService.getById(999L));
    }

    @Test
    void testGetAllByMonthReturnsIncomes() {
        incomeService.create(validIncomeDTO);

        List<IncomeResponseDTO> result = incomeService.getAllByMonth(LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Salary", result.get(0).description());
    }

    @Test
    void testGetByMonthAndTypeReturnsIncomes() {
        incomeService.create(validIncomeDTO);

        List<IncomeResponseDTO> result = incomeService.getByMonthAndType(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), IncomeType.PRINCIPAL);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetTotalByMonthReturnsBigDecimal() {
        incomeService.create(validIncomeDTO);

        BigDecimal result = incomeService.getTotalByMonth(LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetTotalByMonthAndTypeReturnsBigDecimal() {
        incomeService.create(validIncomeDTO);

        BigDecimal result = incomeService.getTotalByMonthAndType(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), IncomeType.PRINCIPAL);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testUpdateValidIncome() {
        IncomeResponseDTO created = incomeService.create(validIncomeDTO);

        CreateIncomeDTO updateDTO = new CreateIncomeDTO(
            "Updated Salary",
            new BigDecimal("3500.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        IncomeResponseDTO result = incomeService.update(created.id(), updateDTO);

        assertNotNull(result);
        assertEquals("Updated Salary", result.description());
    }

    @Test
    void testUpdateNonexistentIncomeThrowsException() {
        CreateIncomeDTO updateDTO = new CreateIncomeDTO(
            "Updated",
            new BigDecimal("1000"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        assertThrows(ResourceNotFoundException.class, () -> incomeService.update(999L, updateDTO));
    }

    @Test
    void testDeleteRemovesIncome() {
        IncomeResponseDTO created = incomeService.create(validIncomeDTO);

        incomeService.delete(created.id());

        assertThrows(ResourceNotFoundException.class, () -> incomeService.getById(created.id()));
    }

    @Test
    void testDeleteNonexistentIncomeThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> incomeService.delete(999L));
    }
}

