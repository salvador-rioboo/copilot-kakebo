package com.kakebo.dto;

import com.kakebo.entity.Category;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseDTO(
    Long id,
    String description,
    BigDecimal amount,
    Category category,
    boolean isFixed,
    boolean isRecurring,
    LocalDate date
) {
    public static ExpenseResponseDTO from(com.kakebo.entity.Expense expense) {
        return new ExpenseResponseDTO(
            expense.getId(),
            expense.getDescription(),
            expense.getAmount(),
            expense.getCategory(),
            expense.isFixed(),
            expense.isRecurring(),
            expense.getDate()
        );
    }
}
