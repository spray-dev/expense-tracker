package com.expensetracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.dto.authentication.AuthenticationResponse;
import com.expensetracker.dto.authentication.LoginAuthenticationRequest;
import com.expensetracker.service.AuthenticationService;

@RestController 
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationResponse toResponse(String token) {
        return new AuthenticationResponse(token);
    }
    
    @PostMapping("/login")
    public AuthenticationResponse login(
        @RequestBody LoginAuthenticationRequest request) {

            String token = authenticationService.authenticate(
                request.email(), 
                request.password()
            );

            return toResponse(token);
    }
}
