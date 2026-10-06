package com.expensetracker.dto.expense.features;

import java.math.BigDecimal;

import com.expensetracker.entity.Category;

public record CategorySpendingSummary(
    Category category,
    BigDecimal total
) {
}
