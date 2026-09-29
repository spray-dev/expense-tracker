package com.expensetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpenseRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveExpense_ShouldPersistExpenseWithUser() {
        User user = new User(
            "coelho",
            "password",
            "coelho@example.com");
        
        User savedUser = userRepository.save(user);
        Expense expense = new Expense(
            "Sushi",
            new BigDecimal("49.99"),
            LocalDateTime.of(2026, 9, 25, 20, 30),
            Category.GIFT,
            savedUser
        );

        Expense savedExpense = expenseRepository.save(expense);

        entityManager.flush();
        entityManager.clear();

        Expense foundExpense = expenseRepository.findById(savedExpense.getId()).get();

        assertThat(foundExpense.getId()).isNotNull();
        assertThat(foundExpense.getDescription()).isEqualTo("Sushi");
        assertThat(foundExpense.getAmount()).isEqualTo(new BigDecimal("49.99"));
        assertThat(foundExpense.getDate()).isEqualTo(LocalDateTime.of(2026, 9, 25, 20, 30));
        assertThat(foundExpense.getCategory()).isEqualTo(Category.GIFT);
        assertThat(foundExpense.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(foundExpense.getUser().getUsername()).isEqualTo("coelho");
        assertThat(foundExpense.getUser()).isNotSameAs(savedUser);
    }
}
