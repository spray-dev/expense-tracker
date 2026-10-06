package com.expensetracker.dto.expense.features;

import java.time.LocalDateTime;

public record DateRange(
    LocalDateTime startDate,
    LocalDateTime endDate
) {
}
