package com.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.repository.ExpenseRepository;

@Service
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;

    private final UserService userService;
    
    public ExpenseService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    public Expense createExpense(String description, BigDecimal amount, LocalDateTime date, Category category, Long userId) {
        User user = userService.getUserById(userId);
        Expense expense = new Expense(description, amount, date, category, user);
        return expenseRepository.save(expense);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
            "Expense with id " + id + " does not exist"));
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Expense updateExpense(Long id, String description, BigDecimal amount, LocalDateTime date, Category category) {
        Expense expense = getExpenseById(id);
        // Only update the fields that are not null
        if (description != null) {
            if (description.isBlank()) {
                throw new IllegalArgumentException("Description cannot be blank");
            }
            expense.setDescription(description);
        }
        if (amount != null) {
            expense.setAmount(amount);
        }
        if (date != null) {
            expense.setDate(date);
        }
        if (category != null) {
            expense.setCategory(category);
        }
        // transactionType and user remain unchanged
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }

}
