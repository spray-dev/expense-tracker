package com.expensetracker.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.expensetracker.entity.PasswordResetToken;
import com.expensetracker.entity.User;
import com.expensetracker.exception.InvalidResourceException;
import com.expensetracker.repository.PasswordResetTokenRepository;

import org.springframework.transaction.annotation.Transactional;

@Service 
public class PasswordResetTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final EmailService emailService;
    private final String frontendUrl;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder, 
        UserService userService, EmailService emailService, @Value("${app.frontend-url}") String frontendUrl) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    public String createResetToken(User user) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = new PasswordResetToken(user, tokenHash, expiresAt);

        passwordResetTokenRepository.save(resetToken);

        return rawToken;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private PasswordResetToken validateToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        PasswordResetToken token = passwordResetTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new InvalidResourceException("Invalid token"));

        if (token.getUsedAt() != null) {
            throw new InvalidResourceException("Token has already been used");
        } 

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResourceException("Token has expired");
        }

        return token;
    }

    @Transactional 
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = validateToken(rawToken);
        User user = token.getUser();

        String hashedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(hashedPassword);
        token.setUsedAt(LocalDateTime.now());
    }

    public void requestPasswordReset(String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        String rawToken = createResetToken(user);

        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }
}
