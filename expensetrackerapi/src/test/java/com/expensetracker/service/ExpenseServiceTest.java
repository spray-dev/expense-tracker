package com.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.InvalidResourceException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.ExpenseRepository;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseServiceTest {

	@Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserService userService;

    private ExpenseService expenseService;

    @BeforeEach 
    void setUp() {
        MockitoAnnotations.openMocks(this);

        expenseService = new ExpenseService(
            expenseRepository, 
            userService
        );
    }
    
    @Test
    void createExpense_ShouldCreateExpenseWithCorrectData() {
        // Arrange

        String description = "Pizza";
        BigDecimal amount = new BigDecimal("49.99");
        LocalDateTime date = LocalDateTime.of(2026, 9, 25, 20, 30);
        Category category = Category.FOOD;
        Long userId = 5L;

        User user = new User(
            "coelho",
            "password",
            "coelho@example.com"
        );

        when(userService.getUserById(userId))
            .thenReturn(user);

        when(expenseRepository.save(any(Expense.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Expense createdExpense = expenseService.createExpense(
            description, 
            amount, 
            date, 
            category, 
            userId
        );

        // Assert
        assertThat(createdExpense).isNotNull();
        assertThat(createdExpense.getDescription()).isEqualTo(description);
        assertThat(createdExpense.getAmount()).isEqualTo(amount);
        assertThat(createdExpense.getDate()).isEqualTo(date);
        assertThat(createdExpense.getCategory()).isEqualTo(category);
        assertThat(createdExpense.getUser()).isSameAs(user);

        // Verify that the expense was saved to the repository
        verify(expenseRepository).save(any(Expense.class));
        verify(userService).getUserById(userId);

    }

    @Test
    void getExpenseById_ShouldReturnExpense_WhenExpenseExists() {
        // Arrange
        Long expenseId = 1L;
        Expense expense = new Expense(
            "Pizza",
            new BigDecimal("49.99"),
            LocalDateTime.of(2026, 9, 25, 20, 30),
            Category.FOOD,
            new User(
                "coelho",
                "password",
                "coelho@example.com"
            )
        );
        
        when(expenseRepository.findById(expenseId))
            .thenReturn(Optional.of(expense));
        // Act
        Expense foundExpense = expenseService.getExpenseById(expenseId);
        // Assert
        assertThat(foundExpense).isSameAs(expense);

        // Verify that the expense was retrieved from the repository
        verify(expenseRepository).findById(expenseId);       
    }

    @Test
    void getExpenseById_ShouldThrowException_WhenExpenseDoesNotExist() {
        // Arrange
        Long expenseId = 1L;
        when(expenseRepository.findById(expenseId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.getExpenseById(expenseId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Expense with id " + expenseId + " does not exist");

        // Verify that the expense was retrieved from the repository
        verify(expenseRepository).findById(expenseId);
    }

    @Test
    void updateExpense_ShouldUpdateAllFields_WhenAllFieldsAreNotNull() {
        // Arrange
        //Create a new expense with old values
        Long expenseId = 1L;
        Expense expense = new Expense(
            "Burguer",
            new BigDecimal("29.99"),
            LocalDateTime.of(2026, 9, 25, 18, 30),
            Category.FOOD,
            new User(
                "coelho",
                "password",
                "coelho@example.com"
            )
        );

        when(expenseRepository.findById(expenseId))
            .thenReturn(Optional.of(expense));
        
        when(expenseRepository.save(any(Expense.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String newDescription = "Sushi";
        BigDecimal newAmount = new BigDecimal("79.99");
        LocalDateTime newDate = LocalDateTime.of(2026, 9, 25, 21, 45);
        Category newCategory = Category.GIFT;

        // Act
        Expense updatedExpense = expenseService.updateExpense(
            expenseId,
            newDescription,
            newAmount,
            newDate,
            newCategory
        );

        // Assert
        assertThat(updatedExpense.getDescription()).isEqualTo(newDescription);
        assertThat(updatedExpense.getAmount()).isEqualTo(newAmount);
        assertThat(updatedExpense.getDate()).isEqualTo(newDate);
        assertThat(updatedExpense.getCategory()).isEqualTo(newCategory);
        assertThat(updatedExpense.getUser()).isSameAs(expense.getUser());

        // Verify that the expense was saved to the repository
        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void updateExpense_ShouldUpdateOnlySpecifiedFields_WhenOnlySpecifiedFieldsAreNotNull() {
        // Arrange
        //Create a new expense with old values
        Long expenseId = 1L;
        BigDecimal originalAmount = new BigDecimal("89.99");
        LocalDateTime originalDate = LocalDateTime.of(2026, 9, 25, 18, 30);
        User originalUser = new User(
            "coelho",
            "password",
            "coelho@example.com"
        );

        Expense expense = new Expense(
            "Burguer",
            originalAmount,
            originalDate,
            Category.FOOD,
            originalUser
        );

        when(expenseRepository.findById(expenseId))
            .thenReturn(Optional.of(expense));
        
        when(expenseRepository.save(any(Expense.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String newDescription = "Sushi";
        Category newCategory = Category.GIFT;

        // Act
        Expense updatedExpense = expenseService.updateExpense(
            expenseId,
            newDescription,
            null,
            null,
            newCategory
        );

        // Assert
        assertThat(updatedExpense.getDescription()).isEqualTo(newDescription);
        assertThat(updatedExpense.getAmount()).isEqualTo(originalAmount);
        assertThat(updatedExpense.getDate()).isEqualTo(originalDate);
        assertThat(updatedExpense.getCategory()).isEqualTo(newCategory);
        assertThat(updatedExpense.getUser()).isSameAs(originalUser);

        // Verify that the expense was saved to the repository
        verify(expenseRepository).findById(expenseId);       
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void updateExpense_ShouldThrowException_WhenDescriptionIsBlank() {
        // Arrange
        Long expenseId = 1L;
        Expense expense = new Expense(
            "Burguer",
            new BigDecimal("29.99"),
            LocalDateTime.of(2026, 9, 25, 18, 30),
            Category.FOOD,
            new User(
                "coelho",
                "password",
                "coelho@example.com"
            )
        );

        when(expenseRepository.findById(expenseId))
            .thenReturn(Optional.of(expense));

        // Act & Assert
        assertThatThrownBy(() -> expenseService.updateExpense(
            expenseId,
            "",
            null,
            null,
            null
        ))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Description cannot be blank");

        // Verify that the expense was saved to the repository
        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteExpense_ShouldDeleteExpense_WhenExpenseExists() {
        // Arrange
        Long expenseId = 1L;
        Expense expense = new Expense(
            "Burguer",
            new BigDecimal("29.99"),
            LocalDateTime.of(2026, 9, 25, 18, 30),
            Category.FOOD,
            new User(
                "coelho",
                "password",
                "coelho@example.com"
            )
        );

        when(expenseRepository.findById(expenseId))
            .thenReturn(Optional.of(expense));

        // Act
        expenseService.deleteExpense(expenseId);

        // Verify that the expense was deleted from the repository
        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository).delete(expense);
    }
}

