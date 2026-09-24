package com.expensetracker.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUsernameRequest(
    @NotBlank(message = "Username cannot be blank")
    String username
) {}
