package com.expensetracker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.expensetracker.dto.expense.CreateExpenseRequest;
import com.expensetracker.dto.expense.ExpenseResponse;
import com.expensetracker.dto.expense.UpdateExpenseRequest;
import com.expensetracker.dto.expense.dashboard.ExpenseDashboardResponse;
import com.expensetracker.dto.expense.features.CategorySpendingSummary;
import com.expensetracker.dto.expense.features.ExpenseAverageSpending;
import com.expensetracker.dto.expense.features.ExpenseCountOfExpenses;
import com.expensetracker.dto.expense.features.ExpenseMonthlyCategorySummaryResponse;
import com.expensetracker.dto.expense.features.ExpenseMonthlyTotalPoint;
import com.expensetracker.dto.expense.features.ExpenseMonthlyTotalResponse;
import com.expensetracker.dto.expense.features.ExpenseYearlySummaryResponse;
import com.expensetracker.dto.expense.features.PageResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.ExpensePeriod;
import com.expensetracker.exception.InvalidResourceException;
import com.expensetracker.service.ExpenseDashboardService;
import com.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    
    private final ExpenseService expenseService;
    private final ExpenseDashboardService expenseDashboardService;

    public ExpenseController(ExpenseService expenseService, ExpenseDashboardService expenseDashboardService) {
        this.expenseService = expenseService;
        this.expenseDashboardService = expenseDashboardService;
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
            expense.getId(), 
            expense.getDescription(), 
            expense.getAmount(), 
            expense.getDate(), 
            expense.getCategory(),
            expense.getUser().getId(), 
            expense.getUser().getUsername(), 
            expense.getUser().getEmail()
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ExpenseResponse> getAllExpenses(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) Category category,
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String direction,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) ExpensePeriod period
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if ((startDate == null) != (endDate == null)) {
            throw new InvalidResourceException(
                "Both startDate and endDate must be provided together."
            );
        }

        if (period != null && (startDate != null || endDate != null)) {
            throw new InvalidResourceException(
                "Use either period or startDate/endDate, not both."
            );
        }

        if (page < 0) {
            throw new InvalidResourceException("Page must be 0 or greater");
        }

        if (size < 1) {
            throw new InvalidResourceException("Size must be at least 1");
        }

        Page<Expense> expensePage = expenseService.filterExpenses(
            userId,
            category,
            startDate,
            endDate,
            description,
            sortBy,
            direction,
            page,
            size,
            period
        );

        Page<ExpenseResponse> responsePage = expensePage.map(this::toResponse);

        return new PageResponse<>(
            responsePage.getContent(), 
            responsePage.getNumber(), 
            responsePage.getSize(), 
            responsePage.getTotalElements(), 
            responsePage.getTotalPages());
    }

    @GetMapping("/monthly-total")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseMonthlyTotalResponse getMonthlyTotal(
        @AuthenticationPrincipal Jwt jwt, 
        @RequestParam int year, 
        @RequestParam int month
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        if (month < 1 || month > 12) {
            throw new InvalidResourceException("Month must be between 1 and 12");
        }

        return new ExpenseMonthlyTotalResponse(
            year, 
            month, 
            expenseService.getMonthlyTotal(userId, year, month)
        );
    }

    @GetMapping("/monthly-category-summary")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseMonthlyCategorySummaryResponse getMonthlyCategorySummary(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year,
        @RequestParam int month
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        if (month < 1 || month > 12) {
            throw new InvalidResourceException("Month must be between 1 and 12");
        }

        return new ExpenseMonthlyCategorySummaryResponse(
            year,
            month,
            expenseService.getMonthlyTotalsByCategory(userId, year, month)
        );
    }

    @GetMapping("/monthly-totals-over-time")
    @ResponseStatus(HttpStatus.OK)
    public List<ExpenseMonthlyTotalPoint> getMonthlyTotalsOverTime(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int startYear,
        @RequestParam int startMonth,
        @RequestParam int endYear,
        @RequestParam int endMonth
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (startYear < 1900 || startYear > 2100 || endYear < 1900 || endYear > 2100) {
            throw new InvalidResourceException("Years must be between 1900 and 2100");
        }

        if (startMonth < 1 || startMonth > 12 || endMonth < 1 || endMonth > 12) {
            throw new InvalidResourceException("Months must be between 1 and 12");
        }

        return expenseService.getMonthlyTotalsOverTime(
            userId, 
            startYear, 
            startMonth, 
            endYear, 
            endMonth);
    }

    @GetMapping("/top-spending-categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategorySpendingSummary> getTopSpendingCategories(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year,
        @RequestParam int month
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        if (month < 1 || month > 12) {
            throw new InvalidResourceException("Month must be between 1 and 12");
        }

        return expenseService.getTopSpendingCategories(
            userId, 
            year, 
            month);
    }

    @GetMapping("/average-spending")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseAverageSpending getAverageSpending(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year,
        @RequestParam int month
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        if (month < 1 || month > 12) {
            throw new InvalidResourceException("Month must be between 1 and 12");
        }

        return new ExpenseAverageSpending(
            year,
            month,
            expenseService.getAverageSpending(userId, year, month)
        );
    }

    @GetMapping("/count-of-expenses")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseCountOfExpenses getCountOfExpenses(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year,
        @RequestParam int month
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        if (month < 1 || month > 12) {
            throw new InvalidResourceException("Month must be between 1 and 12");
        }

        return new ExpenseCountOfExpenses(
            year,
            month,
            expenseService.getCountOfExpenses(userId, year, month)
        );
    }

    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    public List<ExpenseResponse> getRecentExpenses(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "5") int limit
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (limit < 1  || limit > 100) {
            throw new InvalidResourceException("Limit must be between 1 and 100");
        }

        return expenseService.getRecentExpenses(userId, limit)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/largest")
    @ResponseStatus(HttpStatus.OK)
    public List<ExpenseResponse> getLargestExpenses(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "5") int limit
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (limit < 1 || limit > 100) {
            throw new InvalidResourceException("Limit must be between 1 and 100");
        }

        return expenseService.getLargestExpenses(userId, limit)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/yearly-summary")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseYearlySummaryResponse getYearlySummary(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        return expenseService.getYearlySummary(userId, year);
    }

    @GetMapping("/dashboard")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseDashboardResponse getDashboard(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year,
        @RequestParam int month
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        if (year < 1900 || year > 2100) {
            throw new InvalidResourceException("Year must be between 1900 and 2100");
        }

        if (month < 1 || month > 12) {
            throw new InvalidResourceException("Month must be between 1 and 12");
        }

        return expenseDashboardService.getDashboard(userId, year, month);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportExpenses(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());

        List<Expense> expenses = expenseService.getExpensesByUserId(userId);

        String csv = expenseService.exportExpensesToCsv(expenses);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"expenses.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseResponse getExpenseById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = Long.valueOf(jwt.getSubject());
        return toResponse(expenseService.getExpenseByIdAndUserId(id, userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateExpenseRequest request) {
        Long userId = Long.valueOf(jwt.getSubject());
        Expense entity = expenseService.createExpense(
            request.description(), 
            request.amount(), 
            request.date(), 
            request.category(),
            userId
        );
        return toResponse(entity);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseResponse updateExpense(@AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id, 
        @Valid @RequestBody UpdateExpenseRequest request) {
            Long userId = Long.valueOf(jwt.getSubject());
            Expense updatedExpense = expenseService.updateExpense(
                id,
                userId,
                request.description(), 
                request.amount(), 
                request.date(), 
                request.category()
            );
            return toResponse(updatedExpense);
        }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = Long.valueOf(jwt.getSubject());
        expenseService.deleteExpense(id, userId);
    }
}
