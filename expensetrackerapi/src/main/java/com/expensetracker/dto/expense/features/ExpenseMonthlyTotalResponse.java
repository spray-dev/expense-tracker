package com.expensetracker.dto.expense.features;

import java.math.BigDecimal;

public record ExpenseMonthlyTotalResponse(
    int year,
    int month,
    BigDecimal total
) { 
}
