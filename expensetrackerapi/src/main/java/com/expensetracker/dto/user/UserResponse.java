package com.expensetracker.dto.user;

public record UserResponse(
    Long id, 
    String username, 
    String email
) {}
