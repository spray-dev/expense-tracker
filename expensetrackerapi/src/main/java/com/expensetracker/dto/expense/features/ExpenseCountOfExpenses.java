package com.expensetracker.dto.expense.features;

public record ExpenseCountOfExpenses(
    int year,
    int month,
    int count
) {
}
