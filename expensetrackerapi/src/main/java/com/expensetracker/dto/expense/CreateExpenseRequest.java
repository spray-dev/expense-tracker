package com.expensetracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.expensetracker.entity.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateExpenseRequest(
    @NotBlank(message = "Description cannot be blank")
    @Size(max =50, message = "Description must be at most 50 characters long")
    String description,

    @NotNull
    @PositiveOrZero
    BigDecimal amount,

    @NotNull
    LocalDateTime date,

    @NotNull
    Category category
) {
}
