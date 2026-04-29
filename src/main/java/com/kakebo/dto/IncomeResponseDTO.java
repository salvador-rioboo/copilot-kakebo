package com.kakebo.dto;

import com.kakebo.entity.IncomeType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponseDTO(
    Long id,
    String description,
    BigDecimal amount,
    IncomeType type,
    boolean isRecurring,
    LocalDate date
) {
    public static IncomeResponseDTO from(com.kakebo.entity.Income income) {
        return new IncomeResponseDTO(
            income.getId(),
            income.getDescription(),
            income.getAmount(),
            income.getType(),
            income.isRecurring(),
            income.getDate()
        );
    }
}
