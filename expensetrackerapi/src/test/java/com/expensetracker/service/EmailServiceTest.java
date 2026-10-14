package com.expensetracker.service;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EmailServiceTest {
    private MockRestServiceServer server;
    private EmailService emails;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        emails = new EmailService(builder, "sender@example.com", "test-api-key");
    }

    @Test
    void sendsExistingPortugueseEmailThroughBrevoHttps() {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("api-key", "test-api-key"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                {
                  "sender": {"name": "Expense Tracker", "email": "sender@example.com"},
                  "to": [{"email": "recipient@example.com"}],
                  "subject": "Expense Tracker - Redefinir Senha",
                  "textContent": "Você solicitou uma redefinição de senha.\\n\\nUse o link abaixo para criar uma nova senha.\\nhttps://expenses.example.com/reset-password?token=test-token\\n\\nEsse link expira em 30 minutos."
                }
                """))
            .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body("{\"messageId\":\"test-message\"}"));

        emails.sendPasswordResetEmail("recipient@example.com",
            "https://expenses.example.com/reset-password?token=test-token");

        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 429, 500})
    void failsClearlyOnProviderErrorsWithoutExposingResponseBody(int status) {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andRespond(withStatus(HttpStatus.valueOf(status))
                .body("provider response containing sensitive data"));

        assertThatThrownBy(() -> emails.sendPasswordResetEmail("recipient@example.com", "test-link"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Brevo email delivery failed (HTTP " + status + ")");

        server.verify();
    }

    @Test
    void propagatesTransportFailures() {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andRespond(withException(new IOException("Connection timed out")));

        assertThatThrownBy(() -> emails.sendPasswordResetEmail("recipient@example.com", "test-link"))
            .isInstanceOf(ResourceAccessException.class);

        server.verify();
    }

    @Test
    void bootProvidesTheRestClientBuilderForEmailService() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class))
            .withUserConfiguration(EmailService.class)
            .withPropertyValues("app.mail.from=sender@example.com", "brevo.api-key=test-api-key")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(EmailService.class);
            });
    }
}
