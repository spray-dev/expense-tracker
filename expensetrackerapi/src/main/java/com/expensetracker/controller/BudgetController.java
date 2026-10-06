package com.expensetracker.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.dto.budget.BudgetRequest;
import com.expensetracker.dto.budget.BudgetResponse;
import com.expensetracker.service.BudgetService;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping ("/api/budgets/monthly")
public class BudgetController {
    
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public BudgetResponse createOrUpdateBudget(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year,
        @RequestParam int month,
        @Valid @RequestBody BudgetRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        budgetService.createOrUpdateBudget(userId, year, month, request.amount());
        return budgetService.getBudgetStatus(userId, year, month);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public BudgetResponse getBudgetStatus(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam int year,
        @RequestParam int month
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return budgetService.getBudgetStatus(
            userId,
            year,
            month
        );
    }
}
