package com.kakebo.controller;

import com.kakebo.dto.CreateIncomeDTO;
import com.kakebo.entity.IncomeType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IncomeControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IncomeRepository incomeRepository;

    private CreateIncomeDTO createIncomeDTO;

    @BeforeEach
    void setUp() {
        incomeRepository.deleteAll();

        createIncomeDTO = new CreateIncomeDTO(
            "Salary",
            new BigDecimal("3000.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );
    }

    @Test
    void testCreateIncomeReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createIncomeDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("Salary"))
            .andExpect(jsonPath("$.amount").value(3000.00))
            .andExpect(jsonPath("$.type").value("PRINCIPAL"));
    }

    @Test
    void testGetIncomeByIdReturnsOk() throws Exception {
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createIncomeDTO)))
            .andExpect(status().isCreated());

        var incomes = incomeRepository.findAll();
        var incomeId = incomes.get(0).getId();

        mockMvc.perform(get("/api/incomes/" + incomeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Salary"));
    }

    @Test
    void testGetAllIncomesByMonthReturnsOk() throws Exception {
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createIncomeDTO)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/incomes")
                .param("year", String.valueOf(LocalDate.now().getYear()))
                .param("month", String.valueOf(LocalDate.now().getMonthValue())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].description").value("Salary"));
    }

    @Test
    void testGetIncomeByTypeReturnsOk() throws Exception {
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createIncomeDTO)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/incomes/type/PRINCIPAL")
                .param("year", String.valueOf(LocalDate.now().getYear()))
                .param("month", String.valueOf(LocalDate.now().getMonthValue())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].type").value("PRINCIPAL"));
    }

    @Test
    void testCreateIncomeWithInvalidDataReturnsBadRequest() throws Exception {
        CreateIncomeDTO invalidDTO = new CreateIncomeDTO(
            "",
            new BigDecimal("-100"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateIncomeReturnsOk() throws Exception {
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createIncomeDTO)))
            .andExpect(status().isCreated());

        var incomes = incomeRepository.findAll();
        var incomeId = incomes.get(0).getId();

        CreateIncomeDTO updateDTO = new CreateIncomeDTO(
            "Updated Salary",
            new BigDecimal("3500.00"),
            IncomeType.PRINCIPAL,
            true,
            LocalDate.now()
        );

        mockMvc.perform(put("/api/incomes/" + incomeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated Salary"));
    }

    @Test
    void testDeleteIncomeReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/incomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createIncomeDTO)))
            .andExpect(status().isCreated());

        var incomes = incomeRepository.findAll();
        var incomeId = incomes.get(0).getId();

        mockMvc.perform(delete("/api/incomes/" + incomeId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/incomes/" + incomeId))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetNonexistentIncomeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/incomes/999"))
            .andExpect(status().isNotFound());
    }
}
