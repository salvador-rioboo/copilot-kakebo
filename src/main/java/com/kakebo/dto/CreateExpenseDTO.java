package com.kakebo.dto;

import com.kakebo.entity.Category;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseDTO(
    @NotBlank(message = "Description required")
    String description,

    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Category required")
    Category category,

    @NotNull
    Boolean isFixed,

    @NotNull
    Boolean isRecurring,

    @NotNull(message = "Date required")
    LocalDate date
) { }
