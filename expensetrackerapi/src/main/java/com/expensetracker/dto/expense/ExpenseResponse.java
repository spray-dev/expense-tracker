package com.expensetracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.expensetracker.entity.Category;

public record ExpenseResponse(
    Long id,     
    String description, 
    BigDecimal amount, 
    LocalDateTime date, 
    Category category, 
    Long userId, 
    String userUsername, 
    String userEmail
) {
}
