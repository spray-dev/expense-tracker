package com.expensetracker.dto.authentication;

public record LoginAuthenticationRequest(
    String email,
    String password
) {
}
