package com.kakebo.controller;

import com.kakebo.dto.CreateExpenseDTO;
import com.kakebo.entity.Category;
import com.kakebo.repository.ExpenseRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    private CreateExpenseDTO createExpenseDTO;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        createExpenseDTO = new CreateExpenseDTO(
            "Groceries",
            new BigDecimal("125.50"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );
    }

    @Test
    void testCreateExpenseReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExpenseDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("Groceries"))
            .andExpect(jsonPath("$.amount").value(125.50))
            .andExpect(jsonPath("$.category").value("SURVIVAL"));
    }

    @Test
    void testGetExpenseByIdReturnsOk() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExpenseDTO)))
            .andExpect(status().isCreated());

        var expenses = expenseRepository.findAll();
        var expenseId = expenses.get(0).getId();

        mockMvc.perform(get("/api/expenses/" + expenseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Groceries"));
    }

    @Test
    void testGetAllExpensesByMonthReturnsOk() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExpenseDTO)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/expenses")
                .param("year", String.valueOf(LocalDate.now().getYear()))
                .param("month", String.valueOf(LocalDate.now().getMonthValue())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].description").value("Groceries"));
    }

    @Test
    void testCreateExpenseWithInvalidDataReturnsBadRequest() throws Exception {
        CreateExpenseDTO invalidDTO = new CreateExpenseDTO(
            "",
            new BigDecimal("-10"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateExpenseReturnsOk() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExpenseDTO)))
            .andExpect(status().isCreated());

        var expenses = expenseRepository.findAll();
        var expenseId = expenses.get(0).getId();

        CreateExpenseDTO updateDTO = new CreateExpenseDTO(
            "Updated Groceries",
            new BigDecimal("150.00"),
            Category.SURVIVAL,
            false,
            false,
            LocalDate.now()
        );

        mockMvc.perform(put("/api/expenses/" + expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated Groceries"));
    }

    @Test
    void testDeleteExpenseReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExpenseDTO)))
            .andExpect(status().isCreated());

        var expenses = expenseRepository.findAll();
        var expenseId = expenses.get(0).getId();

        mockMvc.perform(delete("/api/expenses/" + expenseId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses/" + expenseId))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetNonexistentExpenseReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/expenses/999"))
            .andExpect(status().isNotFound());
    }
}
