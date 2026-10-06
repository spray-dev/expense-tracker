package com.expensetracker.dto.expense.features;

import java.math.BigDecimal;

public record ExpenseAverageSpending(
    int year,
    int month,
    BigDecimal average
) {
}
