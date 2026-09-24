package com.expensetracker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
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
import com.expensetracker.entity.Expense;
import com.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
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
    public List<ExpenseResponse> getAllExpenses() {
        return expenseService.getAllExpenses()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseResponse getExpenseById(@PathVariable Long id) {
        return toResponse(expenseService.getExpenseById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        Expense entity = expenseService.createExpense(
            request.description(), 
            request.amount(), 
            request.date(), 
            request.category(),
            request.user()
        );
        return toResponse(entity);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseResponse updateExpense(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateExpenseRequest request) {
            Expense updatedExpense = expenseService.updateExpense(
                id,
                request.description(), 
                request.amount(), 
                request.date(), 
                request.category()
            );
            return toResponse(updatedExpense);
        }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }
}
