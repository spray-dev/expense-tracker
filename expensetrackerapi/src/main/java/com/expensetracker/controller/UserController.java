package com.expensetracker.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.dto.CreateUserRequest;
import com.expensetracker.dto.UpdateEmailRequest;
import com.expensetracker.dto.UpdatePasswordRequest;
import com.expensetracker.dto.UpdateUsernameRequest;
import com.expensetracker.dto.UserResponse;
import com.expensetracker.entity.User;
import com.expensetracker.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.http.HttpStatus;

@RestController 
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private UserResponse toResponse(com.expensetracker.entity.User user) {
        return new UserResponse(
            user.getId(), 
            user.getUsername(), 
            user.getEmail()
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserById(@PathVariable Long id) {
        return toResponse(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserByUsername(@PathVariable String username) {
        return toResponse(userService.getUserByUsername(username));
    }

    @GetMapping("/email/{email}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserByEmail(@PathVariable String email) {
        return toResponse(userService.getUserByEmail(email));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User entity = userService.createUser(request.username(), request.email(), request.password());
        return toResponse(entity);
    }

    @PutMapping("/{id}/username")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updateUsername(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateUsernameRequest request) {
            User updatedUser = userService.updateUsername(id, request.username());
            return toResponse(updatedUser);
        }

    @PutMapping("/{id}/email")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updateEmail(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEmailRequest request) {
            User updatedUser = userService.updateEmail(id, request.email());
            return toResponse(updatedUser);
        }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updatePassword(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePasswordRequest request) {
            User updatedUser = userService.updatePassword(id, request.password());
            return toResponse(updatedUser);
        }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
