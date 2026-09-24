package com.expensetracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.expensetracker.entity.Category;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateExpenseRequest(
    String description,
        
    @PositiveOrZero 
    BigDecimal amount,

    LocalDateTime date,

    Category category
) {
}
