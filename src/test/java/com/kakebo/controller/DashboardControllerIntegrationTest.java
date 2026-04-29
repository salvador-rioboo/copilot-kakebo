package com.kakebo.controller;

import com.kakebo.dto.CreateExpenseDTO;
import com.kakebo.dto.CreateIncomeDTO;
import com.kakebo.entity.Category;
import com.kakebo.entity.IncomeType;
import com.kakebo.repository.ExpenseRepository;
import com.kakebo.repository.IncomeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    private int currentYear;
    private int currentMonth;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        incomeRepository.deleteAll();

        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();
    }

    @Test
    void testGetMonthlySummary() throws Exception {
        // Add income
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("5000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incomeDTO)))
            .andExpect(status().isCreated());

        // Add expense
        CreateExpenseDTO expenseDTO = new CreateExpenseDTO(
            "Groceries",
            new BigDecimal("500.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDTO)))
            .andExpect(status().isCreated());

        // Get summary
        mockMvc.perform(get("/api/dashboard/summary")
                .param("year", String.valueOf(currentYear))
                .param("month", String.valueOf(currentMonth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalIncome").value(5000.00))
            .andExpect(jsonPath("$.totalExpenses").value(500.00))
            .andExpect(jsonPath("$.availableMoney").value(4500.00))
            .andExpect(jsonPath("$.plannedSavings").isNumber())
            .andExpect(jsonPath("$.actualSavings").isNumber());
    }

    @Test
    void testGetCurrentMonthlySummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalIncome").isNumber())
            .andExpect(jsonPath("$.totalExpenses").isNumber())
            .andExpect(jsonPath("$.availableMoney").isNumber());
    }

    @Test
    void testHasBudgetAlertReturnsBoolean() throws Exception {
        // Add income
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Small Income",
            new BigDecimal("1000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incomeDTO)))
            .andExpect(status().isCreated());

        // Add large expense to trigger alert
        CreateExpenseDTO expenseDTO = new CreateExpenseDTO(
            "Expensive",
            new BigDecimal("950.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDTO)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/alerts")
                .param("year", String.valueOf(currentYear))
                .param("month", String.valueOf(currentMonth)))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String response = result.getResponse().getContentAsString();
                assert response.equals("true") || response.equals("false");
            });
    }

    @Test
    void testMonthlySummaryWithDefaultParameters() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalIncome").isNumber());
    }

    @Test
    void testMonthlySummaryIncludesExpensesByCategory() throws Exception {
        CreateIncomeDTO incomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("5000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incomeDTO)))
            .andExpect(status().isCreated());

        CreateExpenseDTO expenseDTO = new CreateExpenseDTO(
            "Food",
            new BigDecimal("200.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDTO)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/summary")
                .param("year", String.valueOf(currentYear))
                .param("month", String.valueOf(currentMonth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.expensesByCategory").isMap());
    }
}

