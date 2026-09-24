package com.expensetracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;

public record CreateExpenseRequest(
    String description,
    BigDecimal amount,
    LocalDateTime date,
    Category category,
    User user
) {
}
