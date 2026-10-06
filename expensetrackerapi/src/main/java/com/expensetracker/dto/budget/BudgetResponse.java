package com.expensetracker.dto.budget;

import java.math.BigDecimal;

public record BudgetResponse(
    Long id,
    int year,
    int month,
    BigDecimal budget,
    BigDecimal spent,
    BigDecimal remaining,
    BigDecimal percentageSpent,
    boolean isOverBudget
) {
}
