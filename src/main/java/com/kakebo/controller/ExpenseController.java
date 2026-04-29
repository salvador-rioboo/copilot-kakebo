package com.kakebo.controller;

import com.kakebo.dto.CreateExpenseDTO;
import com.kakebo.dto.ExpenseResponseDTO;
import com.kakebo.entity.Category;
import com.kakebo.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(@Valid @RequestBody CreateExpenseDTO dto) {
        ExpenseResponseDTO created = expenseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> getAllByMonth(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "4") int month) {
        return ResponseEntity.ok(expenseService.getAllByMonth(year, month));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExpenseResponseDTO>> getByCategory(
            @PathVariable Category category,
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "4") int month) {
        return ResponseEntity.ok(expenseService.getByMonthAndCategory(year, month, category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateExpenseDTO dto) {
        return ResponseEntity.ok(expenseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
