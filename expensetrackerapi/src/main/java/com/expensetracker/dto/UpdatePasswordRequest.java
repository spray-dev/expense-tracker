package com.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(
    @NotBlank(message = "Password cannot be blank")
    String password
) {}
