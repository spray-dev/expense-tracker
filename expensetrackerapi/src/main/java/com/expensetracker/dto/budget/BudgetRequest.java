package com.expensetracker.dto.budget;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BudgetRequest(
    @NotNull
    @Positive
    BigDecimal amount 
) {  
}
