package com.expensetracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.expensetracker.entity.Category;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateExpenseRequest(
    @Size(max =50, message = "Description must be at most 50 characters long")
    String description,
        
    @PositiveOrZero 
    BigDecimal amount,

    LocalDateTime date,

    Category category
) {
}
