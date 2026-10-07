package com.expensetracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.expensetracker.dto.budget.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.User;
import com.expensetracker.exception.InvalidResourceException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.BudgetRepository;

import jakarta.transaction.Transactional;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final UserService userService;
    private final ExpenseService expenseService;

    public BudgetService(BudgetRepository budgetRepository, UserService userService, ExpenseService expenseService) {
        this.budgetRepository = budgetRepository;
        this.userService = userService;
        this.expenseService = expenseService;
    }

    public Budget getBudgetByUserIdAndYearAndMonth(Long userId, int year, int month) {
        return budgetRepository.findByUser_IdAndYearAndMonth(userId, year, month).orElseThrow(() -> new ResourceNotFoundException(
            "Budget with userId " + userId + ", year " + year + " and month " + month + " does not exist"));
    }   

    @Transactional
    public Budget createOrUpdateBudget(Long userId, int year, int month, BigDecimal amount) {
        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        if (month < 1 || month > 12) {
            throw new InvalidResourceException("Month must be between 1 and 12");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidResourceException("Amount must be greater than zero");
        }

        User user = userService.getUserById(userId);

        Optional<Budget> existingBudget = budgetRepository.findByUser_IdAndYearAndMonth(userId, year, month);
        if (existingBudget.isPresent()) {
            Budget budget = existingBudget.get();
            budget.setAmount(amount);
            return budgetRepository.save(budget);
        } 

        Budget budget = new Budget(amount, year, month, user);
        return budgetRepository.save(budget);
    }

    public BudgetResponse getBudgetStatus(Long userId, int year, int month) {
        Budget budget = getBudgetByUserIdAndYearAndMonth(userId, year, month);

        return toBudgetResponse(budget, userId);
    }

    public BudgetResponse getBudgetStatusOrNull(Long userId, int year, int month) {
        Optional<Budget> budgetOptional = budgetRepository.findByUser_IdAndYearAndMonth(userId, year, month);
        if (budgetOptional.isEmpty()) {
            return null;
        }

        return toBudgetResponse(budgetOptional.get(), userId);
    }

    private BudgetResponse toBudgetResponse(
            Budget budget,
            Long userId) {
        BigDecimal spent = expenseService.getMonthlyTotal(
                userId,
                budget.getYear(),
                budget.getMonth());

        BigDecimal remaining = budget.getAmount().subtract(spent);

        BigDecimal percentageSpent = spent
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        budget.getAmount(),
                        2,
                        RoundingMode.HALF_UP);

        boolean isOverBudget = spent.compareTo(budget.getAmount()) > 0;

        return new BudgetResponse(
                budget.getId(),
                budget.getYear(),
                budget.getMonth(),
                budget.getAmount(),
                spent,
                remaining,
                percentageSpent,
                isOverBudget);
    }
}
