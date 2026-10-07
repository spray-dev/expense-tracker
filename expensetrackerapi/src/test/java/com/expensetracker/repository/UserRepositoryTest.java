package com.expensetracker.repository;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.Category;
import com.expensetracker.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.expensetracker.entity.User;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserService.class)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void deleteUser_ShouldRemoveUserAndAllOwnedExpensesAndBudget() {
        User user = userRepository.save(new User(
            "delete-me", "password", "delete-me@example.com"));
        Expense firstExpense = expenseRepository.save(new Expense(
            "Lunch", new BigDecimal("25.00"),
            LocalDateTime.of(2026, 10, 7, 12, 0), Category.GIFT, user));
        Expense secondExpense = expenseRepository.save(new Expense(
            "Dinner", new BigDecimal("40.00"),
            LocalDateTime.of(2026, 10, 7, 19, 0), Category.GIFT, user));
        Budget budget = budgetRepository.save(new Budget(
            new BigDecimal("500.00"), 2026, 10, user));

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.existsById(user.getId())).isTrue();
        assertThat(expenseRepository.existsById(firstExpense.getId())).isTrue();
        assertThat(expenseRepository.existsById(secondExpense.getId())).isTrue();
        assertThat(budgetRepository.existsById(budget.getId())).isTrue();

        userService.deleteUser(user.getId());

        // Execute SQL so foreign-key failures cannot hide in pending JPA changes.
        entityManager.flush();
        entityManager.clear();

        assertThat(expenseRepository.findById(firstExpense.getId())).isEmpty();
        assertThat(expenseRepository.findById(secondExpense.getId())).isEmpty();
        assertThat(budgetRepository.findById(budget.getId())).isEmpty();
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void existsByUsernameAndIdNot_ShouldReturnTrue_WhenUsernameAlreadyExists() {
        User user1 = new User(
            "coelho",
            "password",
            "coelho@example.com"
        );
        userRepository.save(user1);

        User user2 = new User(
            "jhon",
            "password",
            "jhon@example.com"
        ); 

        userRepository.save(user2);

        entityManager.flush();
        entityManager.clear();

        assertThat(
            userRepository.existsByUsernameAndIdNot("coelho", user1.getId())
        ).isFalse();

        assertThat(
            userRepository.existsByUsernameAndIdNot("coelho", user2.getId())
        ).isTrue();
    }
}
