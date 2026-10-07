package com.expensetracker.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.BudgetRepository;

class BudgetServiceTest {
    BudgetRepository repository;
    ExpenseService expenses;
    BudgetService service;

    @BeforeEach
    void setUp() {
        repository = mock(BudgetRepository.class);
        expenses = mock(ExpenseService.class);
        service = new BudgetService(repository, mock(UserService.class), expenses);
    }

    @ParameterizedTest
    @CsvSource({"300,100,200,33.33,false", "300,300,0,100.00,false",
        "300,375,-75,125.00,true", "300,0,300,0.00,false", "32,1,31,3.13,false"})
    void calculatesRemainingRoundedPercentageAndOverBudget(
        BigDecimal amount, BigDecimal spent, BigDecimal remaining, BigDecimal percentage, boolean over) {
        Budget budget = new Budget(amount, 2026, 10, new User("alice", "hash", "alice@example.com"));
        budget.setId(7L);
        when(repository.findByUser_IdAndYearAndMonth(42L, 2026, 10)).thenReturn(Optional.of(budget));
        when(expenses.getMonthlyTotal(42L, 2026, 10)).thenReturn(spent);
        var response = service.getBudgetStatus(42L, 2026, 10);
        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(10);
        assertThat(response.budget()).isEqualByComparingTo(amount);
        assertThat(response.spent()).isEqualByComparingTo(spent);
        assertThat(response.remaining()).isEqualByComparingTo(remaining);
        assertThat(response.percentageSpent()).isEqualTo(percentage);
        assertThat(response.isOverBudget()).isEqualTo(over);
        verify(expenses).getMonthlyTotal(42L, 2026, 10);
    }

    @Test
    void missingBudgetIsOptionalForDashboardButNotForExplicitLookup() {
        assertThat(service.getBudgetStatusOrNull(42L, 2026, 10)).isNull();
        assertThatThrownBy(() -> service.getBudgetStatus(42L, 2026, 10))
            .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(expenses);
    }
}
