package com.expensetracker.service;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

import com.expensetracker.config.JwtProperties;

import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.expensetracker.security.AuthenticatedUser;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder,JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.expiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(claims);

        return jwtEncoder.encode(parameters).getTokenValue();
        
    }
}