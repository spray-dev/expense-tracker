package com.expensetracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.dto.authentication.AuthenticationResponse;
import com.expensetracker.dto.authentication.ForgotPasswordRequest;
import com.expensetracker.dto.authentication.LoginAuthenticationRequest;
import com.expensetracker.dto.authentication.MessageResponse;
import com.expensetracker.dto.authentication.ResetPasswordRequest;
import com.expensetracker.dto.user.CreateUserRequest;
import com.expensetracker.dto.user.UserResponse;
import com.expensetracker.service.AuthenticationService;
import com.expensetracker.service.PasswordResetTokenService;
import com.expensetracker.service.UserService;

import jakarta.validation.Valid;
import com.expensetracker.entity.User;
import org.springframework.http.HttpStatus;

@RestController 
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;

    public AuthenticationController(AuthenticationService authenticationService, UserService userService, PasswordResetTokenService passwordResetTokenService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    private AuthenticationResponse toResponse(String token) {
        return new AuthenticationResponse(token);
    }
    
    @PostMapping("/login")
    public AuthenticationResponse login(
        @Valid @RequestBody LoginAuthenticationRequest request) {

            String token = authenticationService.authenticate(
                request.email(), 
                request.password()
            );

            return toResponse(token);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
        @Valid @RequestBody CreateUserRequest request) {
            User user = userService.createUser(
                request.username(), 
                request.email(), 
                request.password()
            );

            return new UserResponse(
                user.getId(), 
                user.getUsername(), 
                user.getEmail()
            );
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {

            passwordResetTokenService.requestPasswordReset(request.email());

            return new MessageResponse("If an account exists for this email, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public void resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {
            passwordResetTokenService.resetPassword(request.token(), request.newPassword());
    }
}
