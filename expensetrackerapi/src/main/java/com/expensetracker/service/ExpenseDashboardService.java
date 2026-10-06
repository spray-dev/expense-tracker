package com.expensetracker.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.expensetracker.dto.budget.BudgetResponse;
import com.expensetracker.dto.expense.dashboard.DashboardExpenseResponse;
import com.expensetracker.dto.expense.dashboard.ExpenseDashboardResponse;
import com.expensetracker.dto.expense.features.CategorySpendingSummary;
import com.expensetracker.entity.Expense;

import java.util.List;
import java.util.Map;

import com.expensetracker.entity.Category;

@Service
public class ExpenseDashboardService {
    
    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    public ExpenseDashboardService(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
    }

    //11. Expense Dashboard
    public ExpenseDashboardResponse getDashboard(Long userId, int year, int month) {
        
        BigDecimal monthlyTotal = expenseService.getMonthlyTotal(userId, year, month);
        BigDecimal averageSpending = expenseService.getAverageSpending(userId, year, month);
        int expenseCount = expenseService.getCountOfExpenses(userId, year, month);
        Map<Category, BigDecimal> categoryTotals = expenseService.getMonthlyTotalsByCategory(userId, year, month);
        List<CategorySpendingSummary> topSpendingCategories = expenseService.getTopSpendingCategories(userId, year, month);
        List<Expense> recentExpenses = expenseService.getRecentExpenses(userId, 5);
        List<Expense> largestExpenses = expenseService.getLargestExpenses(userId, 5);
        BudgetResponse budget = budgetService.getBudgetStatusOrNull(userId, year, month);

        return new ExpenseDashboardResponse(
            year,
            month,
            monthlyTotal,
            averageSpending,
            expenseCount,
            categoryTotals,
            topSpendingCategories,
            recentExpenses.stream().map(this::toDashboardResponse).toList(),
            largestExpenses.stream().map(this::toDashboardResponse).toList(),
            budget
        );
    }

    private DashboardExpenseResponse toDashboardResponse(Expense expense) {
        return new DashboardExpenseResponse(
            expense.getId(), 
            expense.getDescription(), 
            expense.getAmount(), 
            expense.getDate(), 
            expense.getCategory()
        );
    }
}
