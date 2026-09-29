package com.expensetracker.repository;

import org.junit.jupiter.api.Test;

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
