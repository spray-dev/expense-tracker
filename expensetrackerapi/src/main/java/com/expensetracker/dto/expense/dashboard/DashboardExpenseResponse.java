package com.expensetracker.dto.expense.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.expensetracker.entity.Category;

public record DashboardExpenseResponse(

    Long id,
    String description,
    BigDecimal amount,
    LocalDateTime date,
    Category category
) {
}
