package com.kakebo.controller;

import com.kakebo.dto.CreateIncomeDTO;
import com.kakebo.dto.IncomeResponseDTO;
import com.kakebo.entity.IncomeType;
import com.kakebo.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
@CrossOrigin(origins = "*")
public class IncomeController {
    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    public ResponseEntity<IncomeResponseDTO> create(@Valid @RequestBody CreateIncomeDTO dto) {
        IncomeResponseDTO created = incomeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponseDTO>> getAllByMonth(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "4") int month) {
        return ResponseEntity.ok(incomeService.getAllByMonth(year, month));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<IncomeResponseDTO>> getByType(
            @PathVariable IncomeType type,
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "4") int month) {
        return ResponseEntity.ok(incomeService.getByMonthAndType(year, month, type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateIncomeDTO dto) {
        return ResponseEntity.ok(incomeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incomeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
