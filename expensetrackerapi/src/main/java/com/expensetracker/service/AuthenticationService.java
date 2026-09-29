package com.expensetracker.service;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.expensetracker.security.AuthenticatedUser;

import org.springframework.security.core.Authentication;

@Service
public class AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        
        return jwtService.generateToken(user);
    }
}