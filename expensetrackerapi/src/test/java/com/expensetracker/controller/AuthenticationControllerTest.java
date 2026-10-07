package com.expensetracker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.expensetracker.config.*;
import com.expensetracker.entity.User;
import com.expensetracker.repository.*;
import com.expensetracker.service.*;
import tools.jackson.databind.ObjectMapper;

// Real password authentication, token generation/decoding and security filters;
// only persistence is mocked so this web slice needs no database.
@WebMvcTest(controllers = {AuthenticationController.class, UserController.class}, properties = {
    "jwt.secret=VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JKV1RUZXN0aW5nMTIzNDU2Nzg5MA==",
    "jwt.expiration=3600000"
})
@Import({SecurityConfig.class, PasswordConfig.class, AuthenticationService.class,
    JwtService.class, UserService.class, AuthenticationControllerTest.WebSecurity.class})
@EnableConfigurationProperties(JwtProperties.class)
class AuthenticationControllerTest {
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    @org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
    static class WebSecurity {}
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtDecoder decoder;
    @Autowired ObjectMapper json;
    @MockitoBean UserRepository users;
    @MockitoBean ExpenseRepository expenses;
    @MockitoBean BudgetRepository budgets;

    @Test
    void registerLoginAndCurrentUserUseRealPasswordAndBearerToken() throws Exception {
        when(users.save(any(User.class))).thenAnswer(call -> {
            User user = call.getArgument(0);
            user.setId(42L);
            return user;
        });
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"alice","email":"alice@example.com","password":"password123"}
                """))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.email").value("alice@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist());
        var saved = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        User user = saved.getValue();
        assertThat(user.getPassword()).isNotEqualTo("password123");
        assertThat(passwords.matches("password123", user.getPassword())).isTrue();
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(users.findById(42L)).thenReturn(Optional.of(user));
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"alice@example.com","password":"password123"}
                """))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = json.readTree(body).get("token").asText();
        assertThat(decoder.decode(token).getSubject()).isEqualTo("42");
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token)
            .param("userId", "99"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.email").value("alice@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist());
        verify(users, never()).findById(99L);
    }

    @Test
    void wrongPasswordAndUnknownEmailAreUnauthorized() throws Exception {
        User user = new User("alice", passwords.encode("password123"), "alice@example.com");
        user.setId(42L);
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        for (String email : new String[]{"alice@example.com", "unknown@example.com"}) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.token").doesNotExist());
        }
    }

    @Test
    void protectedRoutesRejectMissingAndInvalidBearerTokens() throws Exception {
        for (String path : new String[]{"/api/users/me", "/api/expenses", "/api/expenses/export"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(users, expenses, budgets);
    }

    @Test
    void invalidRegistrationIsRejectedBeforePersistence() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"alice","email":"not-an-email","password":"short"}
                """))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(users);
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws Exception {
        when(users.existsByEmail("alice@example.com")).thenReturn(true);
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"alice","email":"alice@example.com","password":"password123"}
                """))
            .andExpect(status().isConflict());
        verify(users, never()).save(any());
    }
}
