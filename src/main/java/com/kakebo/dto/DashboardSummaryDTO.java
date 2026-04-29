package com.kakebo.dto;

import com.kakebo.entity.Category;
import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryDTO(
    BigDecimal totalIncome,
    BigDecimal totalExpenses,
    BigDecimal availableMoney,
    BigDecimal plannedSavings,
    BigDecimal actualSavings,
    Map<Category, BigDecimal> expensesByCategory
) { }
