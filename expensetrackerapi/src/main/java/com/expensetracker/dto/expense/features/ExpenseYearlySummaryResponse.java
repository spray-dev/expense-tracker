package com.expensetracker.dto.expense.features;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseYearlySummaryResponse(
    int year,
    BigDecimal total,
    List<ExpenseMonthlyTotalResponse> monthlyTotals
) {  
}
