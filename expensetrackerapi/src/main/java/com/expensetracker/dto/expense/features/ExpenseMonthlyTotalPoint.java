package com.expensetracker.dto.expense.features;

import java.math.BigDecimal;

public record ExpenseMonthlyTotalPoint(
    int year,
    int month,
    BigDecimal total
) {   
}
