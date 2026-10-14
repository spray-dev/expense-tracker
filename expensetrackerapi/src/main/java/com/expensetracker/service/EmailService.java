package com.expensetracker.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmailService {

    private final RestClient restClient;
    private final String from;

    public EmailService(RestClient.Builder restClientBuilder,
            @Value("${app.mail.from}") String from,
            @Value("${brevo.api-key}") String apiKey) {
        this.restClient = restClientBuilder
            .baseUrl("https://api.brevo.com")
            .defaultHeader("api-key", apiKey)
            .build();
        this.from = from;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        String textContent =
            "Você solicitou uma redefinição de senha.\n\n" +
            "Use o link abaixo para criar uma nova senha.\n" +
            resetLink +
            "\n\nEsse link expira em 30 minutos.";

        restClient.post()
            .uri("/v3/smtp/email")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of(
                "sender", Map.of("name", "Expense Tracker", "email", from),
                "to", List.of(Map.of("email", to)),
                "subject", "Expense Tracker - Redefinir Senha",
                "textContent", textContent))
            .retrieve()
            .onStatus(status -> status.isError(), (request, response) -> {
                // Do not include provider response bodies, recipients, or reset tokens in errors.
                throw new IllegalStateException("Brevo email delivery failed (HTTP "
                    + response.getStatusCode().value() + ")");
            })
            .toBodilessEntity();
    }
}
