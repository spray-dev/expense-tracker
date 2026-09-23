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
    
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense createExpense(String description, BigDecimal amount, LocalDateTime date, Category category, User user) {
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
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setCategory(category);
        // transactionType and user remain unchanged
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }

}
