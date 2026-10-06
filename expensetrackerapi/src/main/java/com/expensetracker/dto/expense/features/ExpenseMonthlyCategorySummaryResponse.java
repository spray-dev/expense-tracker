package com.expensetracker.dto.expense.features;

import java.math.BigDecimal;
import java.util.Map;

import com.expensetracker.entity.Category;

public record ExpenseMonthlyCategorySummaryResponse(
    int year,
    int month,
    Map<Category, BigDecimal> categoryTotals
) {
}
