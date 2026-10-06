package com.expensetracker.dto.expense.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.expensetracker.dto.budget.BudgetResponse;
import com.expensetracker.dto.expense.features.CategorySpendingSummary;
import com.expensetracker.entity.Category;

public record ExpenseDashboardResponse(
    int year,
    int month,
    BigDecimal monthlyTotal,
    BigDecimal averageSpending,
    int expenseCount,
    Map<Category, BigDecimal> categoryTotals,
    List<CategorySpendingSummary> topSpendingCategories,
    List<DashboardExpenseResponse> recentExpenses,
    List<DashboardExpenseResponse> largestExpenses,
    BudgetResponse budget
) {
}