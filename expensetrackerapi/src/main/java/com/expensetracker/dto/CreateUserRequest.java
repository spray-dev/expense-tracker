package com.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank(message = "Username cannot be blank")
    String username,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not valid")
    String email,

    @NotBlank(message = "Password cannot be blank")
    String password
) {}
