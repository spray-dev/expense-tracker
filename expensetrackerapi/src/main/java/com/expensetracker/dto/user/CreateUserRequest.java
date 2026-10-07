package com.expensetracker.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank(message = "Username cannot be blank")
    String username,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not valid")
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters long")
    String password
) {}
