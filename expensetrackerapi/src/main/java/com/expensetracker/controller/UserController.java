package com.expensetracker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.dto.user.UpdateEmailRequest;
import com.expensetracker.dto.user.UpdatePasswordRequest;
import com.expensetracker.dto.user.UpdateUsernameRequest;
import com.expensetracker.dto.user.UserResponse;
import com.expensetracker.entity.User;
import com.expensetracker.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController 
@RequestMapping("/api/users/me")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(), 
            user.getUsername(), 
            user.getEmail()
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getCurrentUser(
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long currentUserId = Long.valueOf(jwt.getSubject());
        return toResponse(userService.getUserById(currentUserId));
    }

    @PatchMapping("/username")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updateUsername(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdateUsernameRequest request) {
            Long currentUserId = Long.valueOf(jwt.getSubject());
            User updatedUser = userService.updateUsername(currentUserId, request.username());
            return toResponse(updatedUser);
        }

    @PatchMapping("/email")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updateEmail(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdateEmailRequest request) {
            Long currentUserId = Long.valueOf(jwt.getSubject());
            User updatedUser = userService.updateEmail(currentUserId, request.email());
            return toResponse(updatedUser);
        }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updatePassword(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdatePasswordRequest request) {
            Long currentUserId = Long.valueOf(jwt.getSubject());
            User updatedUser = userService.updatePassword(currentUserId, request.password());
            return toResponse(updatedUser);
        }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Long currentUserId = Long.valueOf(jwt.getSubject());
        userService.deleteUser(currentUserId);
    }
}
