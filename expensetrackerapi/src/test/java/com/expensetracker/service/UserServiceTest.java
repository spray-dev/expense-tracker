package com.expensetracker.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.BudgetRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {
    
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @BeforeEach 
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userService = new UserService(
            userRepository, 
            passwordEncoder,
            expenseRepository,
            budgetRepository
        );
    }

    @Test
    void createUser_ShouldHashPasswordBeforeSaving() {
        // Arrange
        String rawPassword = "password";
        String hashedPassword = "hashed-password";

        when(userRepository.existsByUsername("coelho")).thenReturn(false);
        when(userRepository.existsByEmail("coelho@example.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        User createdUser = userService.createUser(
            "coelho", 
            "coelho@example.com", 
            rawPassword
        );
        
        // Assert
        assertThat(createdUser.getPassword()).isNotEqualTo(rawPassword);
        assertThat(createdUser.getPassword()).isEqualTo(hashedPassword);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void updatePassword_ShouldHashPasswordBeforeUpdating() {
        // Arrange
        Long userId = 1L;
        String rawPassword = "password";
        String hashedPassword = "hashed-password";

        User user = new User(
            "coelho",
            "password",
            "coelho@example.com"
        );

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);

        // Act
        User updatedUser = userService.updatePassword(
            userId,
            rawPassword
        );

        // Assert
        assertThat(updatedUser.getPassword()).isNotEqualTo(rawPassword);
        assertThat(updatedUser.getPassword()).isEqualTo(hashedPassword);

        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(rawPassword);
    }
}
