package com.expensetracker.dto.error;

public record ErrorResponse(
    int status,
    String error,
    String message
) {
}
