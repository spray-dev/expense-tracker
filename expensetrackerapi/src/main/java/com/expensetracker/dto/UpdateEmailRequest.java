package com.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailRequest(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not valid")
    String email
) {}
