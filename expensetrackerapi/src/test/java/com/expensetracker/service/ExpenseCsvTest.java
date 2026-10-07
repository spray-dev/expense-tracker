package com.expensetracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.expensetracker.entity.*;
import com.expensetracker.repository.ExpenseRepository;

class ExpenseCsvTest {
    private final ExpenseService service = new ExpenseService(mock(ExpenseRepository.class), mock(UserService.class));

    @Test
    void exportsExactHeaderValuesAndEscapesCommasQuotesAndNewlines() {
        Expense expense = new Expense("Lunch, \"special\"\nsecond line", new BigDecimal("12.50"),
            LocalDateTime.of(2026, 10, 7, 12, 30), Category.FOOD,
            new User("private-user", "private-password", "private@example.com"));
        expense.setId(9L);
        assertThat(service.exportExpensesToCsv(List.of(expense))).isEqualTo(
            "id,description,amount,date,category\n9,\"Lunch, \"\"special\"\"\nsecond line\",12.50,2026-10-07T12:30,FOOD\n");
    }

    @Test
    void emptyExportStillContainsHeader() {
        assertThat(service.exportExpensesToCsv(List.of()))
            .isEqualTo("id,description,amount,date,category\n");
    }
}
