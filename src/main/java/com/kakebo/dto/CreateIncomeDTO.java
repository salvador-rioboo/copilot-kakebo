package com.kakebo.dto;

import com.kakebo.entity.IncomeType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateIncomeDTO(
    @NotBlank(message = "Description required")
    String description,

    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Income type required")
    IncomeType type,

    @NotNull
    Boolean isRecurring,

    @NotNull(message = "Date required")
    LocalDate date
) { }
