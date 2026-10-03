package com.incidentflow.incident_service.controller;

import com.incidentflow.incident_service.dto.LoginRequest;
import com.incidentflow.incident_service.dto.RegisterRequest;
import com.incidentflow.incident_service.dto.TokenResponse;
import com.incidentflow.incident_service.dto.UserResponse;
import com.incidentflow.incident_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
