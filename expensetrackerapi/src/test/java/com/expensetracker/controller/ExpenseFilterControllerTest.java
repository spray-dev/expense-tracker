package com.expensetracker.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.expensetracker.exception.GlobalExceptionHandler;
import com.expensetracker.service.*;

class ExpenseFilterControllerTest {
    @ParameterizedTest
    @ValueSource(strings = {
        "startDate=2024-02-01T00:00:00", "endDate=2024-02-29T23:59:59",
        "period=THIS_MONTH&startDate=2024-02-01T00:00:00&endDate=2024-02-29T23:59:59",
        "page=-1", "size=0", "size=101"
    })
    void invalidDateAndPaginationCombinationsReturnBadRequestBeforeQuery(String query) throws Exception {
        ExpenseService service = mock(ExpenseService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ExpenseController(service, mock(ExpenseDashboardService.class)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "HS256").subject("42").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        try {
            var request = get("/api/expenses");
            for (String parameter : query.split("&")) {
                String[] pair = parameter.split("=", 2);
                request.param(pair[0], pair[1]);
            }
            mvc.perform(request).andExpect(status().isBadRequest());
            verifyNoInteractions(service);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
