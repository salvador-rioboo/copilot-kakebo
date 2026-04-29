package com.kakebo.controller;

import com.kakebo.dto.DashboardSummaryDTO;
import com.kakebo.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getMonthlySummary(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "4") int month) {
        return ResponseEntity.ok(dashboardService.getMonthlySummary(year, month));
    }

    @GetMapping("/summary/current")
    public ResponseEntity<DashboardSummaryDTO> getCurrentMonthlySummary() {
        return ResponseEntity.ok(dashboardService.getCurrentMonthlySummary());
    }

    @GetMapping("/alerts")
    public ResponseEntity<Boolean> hasBudgetAlert(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "4") int month) {
        return ResponseEntity.ok(dashboardService.hasBudgetAlert(year, month));
    }
}
