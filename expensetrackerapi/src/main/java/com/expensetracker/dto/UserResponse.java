package com.expensetracker.dto;

public record UserResponse(
    Long id, 
    String username, 
    String email
) {}
