package com.expensetracker.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.expensetracker.controller.ExpenseController;
import com.expensetracker.entity.*;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.service.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpenseQueryTest {
    @Container @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");
    @Autowired ExpenseRepository expenses;
    @Autowired UserRepository users;

    private ExpenseService service() {
        return new ExpenseService(expenses, mock(UserService.class));
    }
    private User user(String name) {
        return users.saveAndFlush(new User(name, "hash", name + "@example.com"));
    }
    private Expense expense(User user, String description, String amount, LocalDateTime date, Category category) {
        return expenses.saveAndFlush(new Expense(description, new BigDecimal(amount), date, category, user));
    }

    @Test
    void combinesFiltersWithInclusiveDatesSortingPaginationAndUserScope() {
        User alice = user("alice");
        User bob = user("bob");
        LocalDateTime start = LocalDateTime.of(2024, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 2, 29, 23, 59, 59);
        Expense first = expense(alice, "COFFEE start", "10.00", start, Category.FOOD);
        Expense second = expense(alice, "coffee end", "20.00", end, Category.FOOD);
        expense(bob, "Coffee other user", "15.00", start, Category.FOOD);
        expense(alice, "Coffee wrong category", "12.00", start, Category.GIFT);
        expense(alice, "Tea", "12.00", start, Category.FOOD);
        expense(alice, "Coffee before", "12.00", start.minusSeconds(1), Category.FOOD);
        expense(alice, "Coffee after", "12.00", end.plusSeconds(1), Category.FOOD);
        ExpenseService service = service();
        var page0 = service.filterExpenses(alice.getId(), Category.FOOD, start, end,
            "CoFfEe", "amount", "asc", 0, 1, null);
        var page1 = service.filterExpenses(alice.getId(), Category.FOOD, start, end,
            "CoFfEe", "amount", "asc", 1, 1, null);
        assertThat(page0.getContent()).extracting(Expense::getId).containsExactly(first.getId());
        assertThat(page1.getContent()).extracting(Expense::getId).containsExactly(second.getId());
        assertThat(page1.getNumber()).isEqualTo(1);
        assertThat(page1.getSize()).isEqualTo(1);
        assertThat(page1.getTotalElements()).isEqualTo(2);
        assertThat(page1.getTotalPages()).isEqualTo(2);
    }

    @Test
    void defaultsToDescendingDateAndIgnoresBlankDescription() {
        User alice = user("alice");
        User bob = user("bob");
        LocalDateTime date = LocalDateTime.of(2024, 2, 1, 0, 0);
        Expense old = expense(alice, "Old", "1", date, Category.FOOD);
        Expense recent = expense(alice, "Recent", "2", date.plusDays(1), Category.GIFT);
        expense(bob, "Other", "3", date.plusDays(2), Category.FOOD);
        var page = service().filterExpenses(alice.getId(), null, null, null, " ", null, null, 0, 10, null);
        assertThat(page.getContent()).extracting(Expense::getId).containsExactly(recent.getId(), old.getId());
    }

    @ParameterizedTest
    @CsvSource({
        "THIS_MONTH,2024-03-01T00:00:00,2024-03-31T23:59:59",
        "LAST_MONTH,2024-02-01T00:00:00,2024-02-29T23:59:59",
        "LAST_7_DAYS,2024-02-24T00:00:00,2024-03-01T23:59:59",
        "LAST_30_DAYS,2024-02-01T00:00:00,2024-03-01T23:59:59",
        "THIS_YEAR,2024-01-01T00:00:00,2024-12-31T23:59:59"
    })
    void periodsIncludeFullBoundaryDaysAndExcludeOutsideDates(
        ExpensePeriod period, LocalDateTime start, LocalDateTime end) {
        User alice = user("alice");
        User bob = user("bob");
        Expense first = expense(alice, "Start", "1", start, Category.FOOD);
        Expense last = expense(alice, "End", "2", end.withNano(999_999_000), Category.FOOD);
        expense(alice, "Before", "3", start.minusSeconds(1), Category.FOOD);
        expense(alice, "After", "4", end.plusSeconds(1), Category.FOOD);
        expense(bob, "Other user", "5", start, Category.FOOD);
        // Freeze only now(): production has no injectable Clock. Fixed March 1
        // exercises the leap-day and month/year boundaries without timing races.
        LocalDateTime now = LocalDateTime.of(2024, 3, 1, 12, 0);
        try (var time = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            time.when(LocalDateTime::now).thenReturn(now);
            var result = service().filterExpenses(alice.getId(), null, null, null,
                null, null, null, 0, 10, period);
            assertThat(result.getContent()).extracting(Expense::getId).containsExactly(last.getId(), first.getId());
        }
    }

    @Test
    void monthlyTotalIncludesLeapMonthBoundariesAndExcludesOtherUsers() {
        User alice = user("alice");
        User bob = user("bob");
        LocalDateTime start = LocalDateTime.of(2024, 2, 1, 0, 0);
        expense(alice, "First", "10.25", start, Category.FOOD);
        expense(alice, "Last", "20.50", LocalDateTime.of(2024, 2, 29, 23, 59, 59, 999_999_000), Category.FOOD);
        expense(alice, "Before", "100", start.minusSeconds(1), Category.FOOD);
        expense(alice, "After", "100", start.plusMonths(1), Category.FOOD);
        expense(bob, "Other", "100", start, Category.FOOD);
        assertThat(service().getMonthlyTotal(alice.getId(), 2024, 2)).isEqualByComparingTo("30.75");
        assertThat(service().getMonthlyTotal(alice.getId(), 2024, 4)).isEqualByComparingTo("0");
    }

    @ParameterizedTest
    @CsvSource({"2024,2", "2024,12"})
    void dashboardRanksOnlySelectedMonthAndOwnerWithFiveItemLimit(int year, int month) {
        User alice = user("alice");
        User bob = user("bob");
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime next = start.plusMonths(1);
        var own = new java.util.ArrayList<Expense>();
        for (int i = 0; i < 6; i++) {
            own.add(expense(alice, "Selected " + i, Integer.toString(60 - i),
                i == 5 ? next.minusNanos(1000) : start.plusDays(i), Category.FOOD));
        }
        expense(alice, "Before", "9999", start.minusSeconds(1), Category.FOOD);
        Expense following = expense(alice, "Next month", "9999", next, Category.FOOD);
        expense(alice, "Other year", "9999", start.minusYears(1), Category.FOOD);
        expense(bob, "Private", "99999", next.minusNanos(1000), Category.FOOD);
        ExpenseDashboardService dashboard = new ExpenseDashboardService(service(), mock(BudgetService.class));

        var selected = dashboard.getDashboard(alice.getId(), year, month);
        assertThat(selected.recentExpenses()).extracting(e -> e.id()).containsExactly(
            own.get(5).getId(), own.get(4).getId(), own.get(3).getId(), own.get(2).getId(), own.get(1).getId());
        assertThat(selected.largestExpenses()).extracting(e -> e.id()).containsExactly(
            own.get(0).getId(), own.get(1).getId(), own.get(2).getId(), own.get(3).getId(), own.get(4).getId());
        var followingMonth = dashboard.getDashboard(alice.getId(), next.getYear(), next.getMonthValue());
        assertThat(followingMonth.recentExpenses()).extracting(e -> e.id()).containsExactly(following.getId());
        assertThat(followingMonth.largestExpenses()).extracting(e -> e.id()).containsExactly(following.getId());
        var empty = dashboard.getDashboard(alice.getId(), next.plusMonths(1).getYear(), next.plusMonths(1).getMonthValue());
        assertThat(empty.recentExpenses()).isEmpty();
        assertThat(empty.largestExpenses()).isEmpty();
    }

    @Test
    void anotherUsersExpenseCannotBeReadUpdatedOrDeleted() {
        User alice = user("alice");
        User bob = user("bob");
        Expense owned = expense(bob, "Private", "99.99", LocalDateTime.of(2024, 2, 1, 0, 0), Category.FOOD);
        ExpenseService service = service();
        assertThatThrownBy(() -> service.getExpenseByIdAndUserId(owned.getId(), alice.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.updateExpense(owned.getId(), alice.getId(), "Changed", null, null, null))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.deleteExpense(owned.getId(), alice.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(expenses.findByIdAndUser_Id(owned.getId(), bob.getId())).isPresent();
        assertThat(expenses.findById(owned.getId()).orElseThrow().getDescription()).isEqualTo("Private");
    }

    @Test
    void csvEndpointExportsOnlyAuthenticatedUsersRowsWithDownloadHeaders() throws Exception {
        User alice = user("alice");
        User bob = user("bob");
        Expense own = expense(alice, "Own lunch", "12.50", LocalDateTime.of(2024, 2, 1, 12, 30), Category.FOOD);
        expense(bob, "Private other user", "99.99", own.getDate(), Category.FOOD);
        var mvc = MockMvcBuilders.standaloneSetup(new ExpenseController(service(), mock(ExpenseDashboardService.class)))
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "HS256").subject(alice.getId().toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        try {
            mvc.perform(get("/api/expenses/export").param("userId", bob.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"expenses.csv\""))
                .andExpect(content().string("id,description,amount,date,category\n" + own.getId()
                    + ",\"Own lunch\",12.50,2024-02-01T12:30,FOOD\n"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
