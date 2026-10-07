package com.expensetracker.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;


import org.springframework.http.MediaType;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.service.ExpenseDashboardService;
import com.expensetracker.service.ExpenseService;

import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class ExpenseControllerTest {
    
    private MockMvc mockMvc;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private ExpenseDashboardService expenseDashboardService;

    @BeforeEach 
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        mockMvc = MockMvcBuilders
        .standaloneSetup(new ExpenseController(expenseService, expenseDashboardService))
        .setCustomArgumentResolvers(
            new AuthenticationPrincipalArgumentResolver()
        )
        .build();
    }

    private void authenticateAs(Long userId) {
        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .subject(userId.toString())
            .build();

        Authentication authentication =
            new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getExpenseById_ShouldReturnExpense_WhenExpenseExists() throws Exception {
        // Arrange
        Long expenseId = 1L;
        Long userId = 5L;

        authenticateAs(userId);

        User user = new User(
            "coelho",
            "password",
            "coelho@example.com"
        );

        BigDecimal amount = new BigDecimal("49.99");
        LocalDateTime date = LocalDateTime.of(2026, 9, 25, 20, 30);
        

        Expense expense = new Expense(
            "Pizza",
            amount,
            date,
            Category.FOOD,
            user
        );

        expense.setId(expenseId);
        user.setId(userId);

        when(expenseService.getExpenseByIdAndUserId(expenseId, userId))
            .thenReturn(expense);

        // Act
        mockMvc.perform(
            get("/api/expenses/{id}", expenseId))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expenseId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Pizza"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(49.99))
            .andExpect(MockMvcResultMatchers.jsonPath("$.date").value("2026-09-25T20:30:00"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("FOOD"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.userUsername").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.userEmail").doesNotExist());

        // Verify that the expense was retrieved from the service
        verify(expenseService).getExpenseByIdAndUserId(expenseId, userId);
    }

    @Test
    void createExpense_ShouldReturnCreatedExpense_WhenRequestIsValid() throws Exception {
        // Arrange
        // What objects/data do I need?
        Long expenseId = 1L;
        Long userId = 5L;

        authenticateAs(userId);

        User user = new User(
            "coelho",
            "password",
            "coelho@example.com"
        );

        BigDecimal amount = new BigDecimal("49.99");
        LocalDateTime date = LocalDateTime.of(2026, 9, 25, 20, 30);

        Expense expense = new Expense(
            "Pizza",
            amount,
            date,
            Category.FOOD,
            user
        );

        expense.setId(expenseId);
        user.setId(userId);

        when(expenseService.createExpense(
            "Pizza",
            amount,
            date,
            Category.FOOD,
            userId
        ))
            .thenReturn(expense);
        // Act & Assert
        // What HTTP request am I making?
        mockMvc.perform(
            post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {   
                            "description": "Pizza",
                            "amount": 49.99,
                            "date": "2026-09-25T20:30:00",
                            "category": "FOOD"
                        }
                        """)
        )
        // What status should come back?
        .andExpect(MockMvcResultMatchers.status().isCreated())
        // What should the JSON response contain?
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expenseId))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Pizza"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(49.99))
        .andExpect(MockMvcResultMatchers.jsonPath("$.date").value("2026-09-25T20:30:00"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("FOOD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.userId").doesNotExist())
        .andExpect(MockMvcResultMatchers.jsonPath("$.userUsername").doesNotExist())
        .andExpect(MockMvcResultMatchers.jsonPath("$.userEmail").doesNotExist());
        // Verify
        // What exact service method should have been called?
        verify(expenseService).createExpense(
            "Pizza",
            amount,
            date,
            Category.FOOD,
            userId
        );
    }

    @Test
    void createExpense_ShouldReturnBadRequest_WhenDescriptionIsBlank() throws Exception {
        // Act & Assert
        // What objects/data do I need?
        authenticateAs(5L);
        mockMvc.perform(
            post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {   
                            "description": "",
                            "amount": 49.99,
                            "date": "2026-09-25T20:30:00",
                            "category": "FOOD"
                        }
                        """)
        )
        // What status should come back?
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // Verify
        verifyNoInteractions(expenseService);
        
    }

    @Test
    void updateExpense_ShouldReturnUpdatedExpense_WhenRequestIsValid() throws Exception {
        // Arrange
        // What objects/data do I need?
        Long expenseId = 1L;
        Long userId = 5L;

        authenticateAs(userId);

        User user = new User(
            "coelho",
            "password",
            "coelho@example.com"
        );

        BigDecimal amount = new BigDecimal("49.99");
        LocalDateTime date = LocalDateTime.of(2026, 9, 25, 20, 30);

        Expense expense = new Expense(
            "Sushi",
            amount,
            date,
            Category.GIFT,
            user
        );

        expense.setId(expenseId);
        user.setId(userId);

        when(expenseService.updateExpense(
            expenseId,
            userId,
            "Sushi",
            null,
            null,
            Category.GIFT
        ))
            .thenReturn(expense);

        // Act & Assert
        // What HTTP request am I making?
        mockMvc.perform(
            patch("/api/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {   
                            "description": "Sushi",
                            "category": "GIFT"
                        }
                        """)
        )
        // What status should come back?
        .andExpect(MockMvcResultMatchers.status().isOk())
        // What should the JSON response contain?
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expenseId))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Sushi"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("GIFT"));

        // Verify
        // What exact service method should have been called?
        verify(expenseService).updateExpense(
            expenseId,
            userId,
            "Sushi",
            null,
            null,
            Category.GIFT
        );
    }
}


